/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import javax.annotation.Nullable;
import java.util.Set;

@Rule(
  key = GenericExceptionCheck.KEY,
  name = "Generic exceptions ErrorException, RuntimeException and Exception should not be thrown",
  priority = Priority.MAJOR,
  tags = {Tags.CWE, Tags.ERROR_HANDLING, Tags.SECURITY})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.EXCEPTION_HANDLING)
@SqaleConstantRemediation("20min")
public class GenericExceptionCheck extends PHPVisitorCheck {

  public static final String KEY = "S112";
  public static final String MESSAGE = "Define and throw a dedicated exception instead of using a generic one.";

  private static final Set<String> RAW_EXCEPTIONS = ImmutableSet.of("ErrorException", "RuntimeException", "Exception");
  private Set<String> importedGenericExceptions = Sets.newHashSet();
  private String currentNamespace = "";

  @Override
  public void visitScript(ScriptTree tree) {
    importedGenericExceptions.clear();
    currentNamespace = "";
    super.visitScript(tree);
  }

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    NamespaceNameTree namespaceName = getThrownClassName(tree);

    if (namespaceName != null && isGenericException(namespaceName)) {
      context().newIssue(KEY, MESSAGE).tree(namespaceName);
    }

    super.visitThrowStatement(tree);
  }

  @Override
  public void visitUseClause(UseClauseTree tree) {
    String name = tree.namespaceName().name().text();

    if (tree.namespaceName().namespaces().isEmpty() && RAW_EXCEPTIONS.contains(name)) {
      importedGenericExceptions.add(tree.alias() != null ? tree.alias().text() : name);
    }

    super.visitUseClause(tree);
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    if (tree.namespaceName() != null) {
      currentNamespace = tree.namespaceName().fullName();
    }

    super.visitNamespaceStatement(tree);

    // Delimited namespace with curly braces
    if (tree.openCurlyBrace() != null) {
      currentNamespace = "";
    }
  }

  private boolean isGenericException(NamespaceNameTree namespaceName) {
    return isImportedGenericException(namespaceName) || isGlobalNamespaceGenericException(namespaceName);

  }

  private boolean isImportedGenericException(NamespaceNameTree namespaceName) {
    return importedGenericExceptions.contains(namespaceName.fullName());
  }

  private boolean isGlobalNamespaceGenericException(NamespaceNameTree namespaceName) {
    return isFromGlobalNamespace(namespaceName) && RAW_EXCEPTIONS.contains(namespaceName.name().text());
  }

  private boolean isFromGlobalNamespace(NamespaceNameTree namespaceName) {
      return !namespaceName.hasQualifiers()
        && (namespaceName.isFullyQualified() || currentNamespace.isEmpty());
  }

  @Nullable
  private static NamespaceNameTree getThrownClassName(ThrowStatementTree tree) {
    if (tree.expression().is(Kind.NEW_EXPRESSION)) {
      NewExpressionTree newExpression = (NewExpressionTree) tree.expression();

      if (newExpression.expression().is(Kind.FUNCTION_CALL)) {
        FunctionCallTree functionCall = (FunctionCallTree) newExpression.expression();

        if (functionCall.callee().is(Kind.NAMESPACE_NAME)) {
          return (NamespaceNameTree) functionCall.callee();
        }
      }
    }
    return null;
  }

}
