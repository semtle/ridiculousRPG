/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menu) {
	if (keycode == Keys.ESCAPE) {
		$.exit();
		return true;
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menu) {
	var skin = menu.skinNormal;
	var w = new ui.Window("Start menu", skin);
	menu.addGUIcomponent(w);

	var resume = new ui.TextButton("Continue at last save point", skin);
	resume.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.changeState(MENU_STATE_IDLE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(resume);

	var load = new ui.TextButton("Load game", skin);
	load.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.showInfoFocused("Load is not implemented yet.\n"
					+ "This is an early alpha release!");
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(load);
	
	var start = new ui.TextButton("Start new game", skin);
	start.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			menu.changeState(MENU_STATE_IDLE);
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(start);

	var toggleFull = new ui.TextButton(
			$.isFullscreen() ? "Window mode" : "Fullscreen mode", skin);
	toggleFull.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			$.toggleFullscreen();
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(toggleFull);

	var exit = new ui.TextButton("Exit game (Esc)", skin);
	exit.clickListener = new ui.ClickListener() {
		click: function (actor, x, y) {
			$.exit();
		}
	};
	w.row().fill(true, true).expand(true, false);
	w.add(exit);

	w.pack();
	menu.center(w);
	menu.focus(resume);
}