/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = EchoWithParenthesisCheck.KEY)
public class EchoWithParenthesisCheck extends FunctionUsageCheck {

  public static final String KEY = "S2041";
  private static final String MESSAGE = "Remove the parentheses from this \"echo\" call.";

  @Override
  protected Set<String> lookedUpFunctionNames() {
    return SetUtils.immutableSetOf("echo");
  }

  @Override
  protected void checkFunctionCall(FunctionCallTree tree) {
    if (isParenthesized(tree)) {
      newIssue(tree.callee(), MESSAGE);
    }
  }

  private static boolean isParenthesized(FunctionCallTree tree) {
    return tree.callArguments().size() == 1 && tree.callArguments().get(0).value().is(Tree.Kind.PARENTHESISED_EXPRESSION);
  }

}
