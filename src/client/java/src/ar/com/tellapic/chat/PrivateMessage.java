package ar.com.tellapic.chat;
///**
// *   Copyright (c) 2010 Sebastián Treu.
// *
// *   This program is free software; you can redistribute it and/or modify
// *   it under the terms of the GNU General Public License as published by
// *   the Free Software Foundation; version 2 of the License.
// *
// *   This program is distributed in the hope that it will be useful,
// *   but WITHOUT ANY WARRANTY; without even the implied warranty of
// *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *   GNU General Public License for more details.
// *
// * @author
// *         Sebastian Treu 
// *         sebastian.treu(at)gmail.com
// *
// */  
//package com.tellapic.chat;
//
///**
// * @author 
// *          Sebastian Treu
// *          sebastian.treu(at)gmail.com
// *
// */
//public class PrivateMessage extends Message {
//
//	private String to;
//	
//	/**
//	 * @param text
//	 * @param recipients
//	 * @param from
//	 */
//	public PrivateMessage(String from, String to, String text) {
//		super(from, text);
//		if (to == null)
//			throw new IllegalArgumentException("Private message must have one receiver");
//		this.to = to;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.tellapic.chat.Message#isPrivate()
//	 */
//	@Override
//	public boolean isPrivate() {
//		return true;
//	}
//}
