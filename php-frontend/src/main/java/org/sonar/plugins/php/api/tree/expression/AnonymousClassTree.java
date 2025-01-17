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
package org.sonar.plugins.php.api.tree.expression;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/language.oop5.anonymous.php">Anonymous class</a>
 *
 * <pre>
 *   class
 * </pre>
 */
public interface AnonymousClassTree extends ExpressionTree, ClassTree {

  @Override
  SyntaxToken classToken();

  @Nullable
  SyntaxToken openParenthesisToken();

  /**
   * @deprecated since 3.11 . Use {@link #callArguments()} instead.
   */
  @Deprecated
  SeparatedList<ExpressionTree> arguments();

  SeparatedList<CallArgumentTree> callArguments();

  @Nullable
  SyntaxToken closeParenthesisToken();

  @Override
  @Nullable
  SyntaxToken extendsToken();

  @Override
  @Nullable
  NamespaceNameTree superClass();

  @Override
  @Nullable
  SyntaxToken implementsToken();

  @Override
  SeparatedList<NamespaceNameTree> superInterfaces();

  @Override
  SyntaxToken openCurlyBraceToken();

  @Override
  List<ClassMemberTree> members();

  @Override
  SyntaxToken closeCurlyBraceToken();

}
