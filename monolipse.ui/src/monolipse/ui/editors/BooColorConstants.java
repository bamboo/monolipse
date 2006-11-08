/*
 * Boo Development Tools for the Eclipse IDE
 * Copyright (C) 2005 Rodrigo B. de Oliveira (rbo@acm.org)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
package monolipse.ui.editors;

import org.eclipse.swt.graphics.*;

public interface BooColorConstants {	
	RGB COMMENT = new RGB(128, 128, 128);
	RGB DIRECTIVE = new RGB(158, 158, 158);
	RGB STRING = new RGB(0, 128, 255);
	RGB TRIPLE_QUOTED_STRING = new RGB(0, 128, 0);
	RGB KEYWORD = new RGB(0, 0, 255);
	RGB MEMBER = new RGB(0, 0, 128);
	RGB MODIFIER = new RGB(170, 50, 50);
	RGB PRIMITIVE = new RGB(0, 153, 52);
	RGB LITERAL = new RGB(0, 0, 0);
	RGB BUILTIN = new RGB(10, 172, 10);
	RGB BACKGROUND = new RGB(255, 255, 255);
	RGB DEFAULT = new RGB(0, 0, 0);
	RGB NUMBER = new RGB(0, 0, 128);
	RGB INVOCATION = new RGB(15, 15, 155);
	RGB NAMESPACE = new RGB(0, 128, 0);
	RGB OPERATORS = new RGB(0x55, 0x55, 0xbb);
	RGB REGEX = new RGB(0xFF, 0x66, 00);
}
