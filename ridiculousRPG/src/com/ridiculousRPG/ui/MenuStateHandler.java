/*
 * Copyright 2011 Alexander Baumgartner
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

package com.ridiculousRPG.ui;

import java.io.Serializable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Disposable;

/**
 * This interface is used to connect state handler with their menu service.<br>
 * Every menu service needs at least one state. The so called &quot;idle&quot;
 * state which indicates that the game is running and the menu service is
 * listening for user input. The &quot;idle&quot; state should not freeze the
 * world ({@link #isFreezeTheWorld()}=false) or clear the screen (
 * {@link #isClearTheScreen()}=false) but it should close all the other (open)
 * menus ({@link #isClearTheMenu()}=true).
 * 
 * @see MenuService
 * @author Alexander Baumgartner
 */
public interface MenuStateHandler extends Disposable, Serializable {
	/**
	 * This method is automatically called by the corresponding
	 * {@link MenuService} if the menu is in this state and a key is released
	 * (keyUp). Define your keyboard shortcuts inside this method.
	 * 
	 * @param keycode
	 *            The key which is actually released (keyUp)
	 * @param menu
	 *            The {@link MenuService} associated with this state.
	 * @return true if the keyUp event has been consumed
	 */
	public boolean processInput(int keycode, MenuService menuService);

	/**
	 * This method is automatically called by the corresponding
	 * {@link MenuService} if it changes into this state.
	 * 
	 * @param menuService
	 *            The {@link MenuService} associated with this state.
	 */
	public void createGui(MenuService menuService);

	/**
	 * Return true if you want to freeze the world while this menu is open.
	 * 
	 * @return true if the world should be frozen while this menu state is
	 *         active.
	 */
	public boolean isFreezeTheWorld();

	/**
	 * Return true if you want a blank black background while this menu is open.<br>
	 * If you want a black background you have to freeze the world (
	 * {@link #isFreezeTheWorld()}=true).
	 * 
	 * @return true if the background screen should be black while this menu
	 *         state is active.
	 */
	public boolean isClearTheScreen();

	/**
	 * Return true if you want to close all the other menus.<br>
	 * This should be used for the &quot;idle&quot; state while the game is
	 * running and the {@link MenuService} is listening for input.
	 * 
	 * @return true if all the open menus should be closed when switching into
	 *         this state.
	 */
	public boolean isClearTheMenu();

	/**
	 * Returns whether the BACK button on Android should be caught. This will
	 * prevent the app from being paused. Will have no effect on the desktop.
	 * 
	 * @see Gdx#input
	 * @see Input#setCatchBackKey(boolean)
	 * @param catchBack
	 *            whether to catch the back button
	 */
	public boolean isCatchBackKey();

	/**
	 * Returns whether the MENU button on Android should be caught. This will
	 * prevent the onscreen keyboard to show up. Will have no effect on the
	 * desktop.
	 * 
	 * @see Gdx#input
	 * @see Input#setCatchMenuKey(boolean)
	 * @param catchMenu
	 *            whether to catch the menu button
	 */
	public boolean isCatchMenuKey();

	/**
	 * Returns the unique id for this menu state.<br>
	 * Each state needs an unique id to identify it.
	 */
	public int getStateId();

	/**
	 * Frees all resources after closing the menu
	 */
	public void freeResources();
}
