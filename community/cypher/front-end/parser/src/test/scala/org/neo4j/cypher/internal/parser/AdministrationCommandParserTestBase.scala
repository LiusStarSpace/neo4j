/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.parser

import java.nio.charset.StandardCharsets

import org.neo4j.cypher.internal.ast
import org.neo4j.cypher.internal.ast.ActionResource
import org.neo4j.cypher.internal.ast.AdminAction
import org.neo4j.cypher.internal.ast.AstConstructionTestSupport
import org.neo4j.cypher.internal.ast.DatabaseAction
import org.neo4j.cypher.internal.ast.GraphScope
import org.neo4j.cypher.internal.ast.PrivilegeQualifier
import org.neo4j.cypher.internal.ast.PrivilegeType
import org.neo4j.cypher.internal.ast.RevokeBothType
import org.neo4j.cypher.internal.ast.RevokeDenyType
import org.neo4j.cypher.internal.ast.RevokeGrantType
import org.neo4j.cypher.internal.expressions
import org.neo4j.cypher.internal.expressions.Parameter
import org.neo4j.cypher.internal.util.InputPosition
import org.neo4j.cypher.internal.util.symbols.CTString
import org.parboiled.scala.Rule1

class AdministrationCommandParserTestBase
  extends ParserAstTest[ast.Statement] with Statement with AstConstructionTestSupport {

  implicit val parser: Rule1[ast.Statement] = Statement

  def literal(name: String): Either[String, expressions.Parameter] = Left(name)

  def param(name: String): Either[String, expressions.Parameter] = Right(expressions.Parameter(name, CTString)(_))

  def toUtf8Bytes(pw: String): Array[Byte] = pw.getBytes(StandardCharsets.UTF_8)

  def pw(password: String) = expressions.SensitiveStringLiteral(toUtf8Bytes(password))(_)

  def pwParam(name: String): expressions.Parameter = expressions.Parameter(name, CTString)(_)

  type resourcePrivilegeFunc = (PrivilegeType, ActionResource, List[GraphScope], PrivilegeQualifier, Seq[Either[String, Parameter]]) => InputPosition => ast.Statement
  type noResourcePrivilegeFunc = (PrivilegeType, List[GraphScope], PrivilegeQualifier, Seq[Either[String, Parameter]]) => InputPosition => ast.Statement
  type databasePrivilegeFunc = (DatabaseAction, List[GraphScope], Seq[Either[String, Parameter]]) => InputPosition => ast.Statement
  type transactionPrivilegeFunc = (DatabaseAction, List[GraphScope], PrivilegeQualifier, Seq[Either[String, Parameter]]) => InputPosition => ast.Statement
  type dbmsPrivilegeFunc = (AdminAction, Seq[Either[String, Parameter]]) => InputPosition => ast.Statement

  def grant(p: PrivilegeType, a: ActionResource, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.GrantPrivilege(p, Some(a), s, q, r)

  def grant(p: PrivilegeType, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.GrantPrivilege(p, None, s, q, r)

  def grantDatabasePrivilege(d: DatabaseAction, s: List[GraphScope], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.GrantPrivilege.databaseAction(d, s, r)

  def grantTransactionPrivilege(d: DatabaseAction, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.GrantPrivilege.databaseAction(d, s, r, q)

  def grantDbmsPrivilege(a: AdminAction, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.GrantPrivilege.dbmsAction(a, r)

  def deny(p: PrivilegeType, a: ActionResource, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.DenyPrivilege(p, Some(a), s, q, r)

  def deny(p: PrivilegeType, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.DenyPrivilege(p, None, s, q, r)

  def denyDatabasePrivilege(d: DatabaseAction, s: List[GraphScope], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.DenyPrivilege.databaseAction(d, s, r)

  def denyTransactionPrivilege(d: DatabaseAction, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.DenyPrivilege.databaseAction(d, s, r, q)

  def denyDbmsPrivilege(a: AdminAction, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.DenyPrivilege.dbmsAction(a, r)

  def revokeGrant(p: PrivilegeType, a: ActionResource, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege(p, Some(a), s, q, r, RevokeGrantType()(pos))

  def revokeGrant(p: PrivilegeType, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege(p, None, s, q, r, RevokeGrantType()(pos))

  def revokeGrantDatabasePrivilege(d: DatabaseAction, s: List[GraphScope], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.databaseGrantedAction(d, s, r)

  def revokeGrantTransactionPrivilege(d: DatabaseAction, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.databaseGrantedAction(d, s, r, q)

  def revokeGrantDbmsPrivilege(a: AdminAction, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.grantedDbmsAction(a, r)

  def revokeDeny(p: PrivilegeType, a: ActionResource, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege(p, Some(a), s, q, r, RevokeDenyType()(pos))

  def revokeDeny(p: PrivilegeType, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege(p, None, s, q, r, RevokeDenyType()(pos))

  def revokeDenyDatabasePrivilege(d: DatabaseAction, s: List[GraphScope], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.databaseDeniedAction(d, s, r)

  def revokeDenyTransactionPrivilege(d: DatabaseAction, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.databaseDeniedAction(d, s, r, q)

  def revokeDenyDbmsPrivilege(a: AdminAction, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.deniedDbmsAction(a, r)

  def revokeBoth(p: PrivilegeType, a: ActionResource, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege(p, Some(a), s, q, r, RevokeBothType()(pos))

  def revokeBoth(p: PrivilegeType, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege(p, None, s, q, r, RevokeBothType()(pos))

  def revokeDatabasePrivilege(d: DatabaseAction, s: List[GraphScope], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.databaseAction(d, s, r)

  def revokeTransactionPrivilege(d: DatabaseAction, s: List[GraphScope], q: PrivilegeQualifier, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.databaseAction(d, s, r, q)

  def revokeDbmsPrivilege(a: AdminAction, r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r)
}
