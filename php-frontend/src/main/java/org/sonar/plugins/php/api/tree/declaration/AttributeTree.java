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
package org.sonar.plugins.php.api.tree.declaration;

import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import javax.annotation.Nullable;

/**
 * <a href="https://www.php.net/manual/de/language.attributes.syntax.php">Attributes</a>
 * <pre>
 *   {@link #name()}
 *   {@link #name()}()
 *   {@link #name()}( {@link #arguments()} )
 * </pre>
 */
public interface AttributeTree extends Tree {

  NamespaceNameTree name();

  @Nullable
  SyntaxToken openParenthesisToken();

  SeparatedList<CallArgumentTree> arguments();

  @Nullable
  SyntaxToken closeParenthesisToken();

}
