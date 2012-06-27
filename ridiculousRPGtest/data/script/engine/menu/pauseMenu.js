/*
 * This script file contains callback functions used by
 * com.madthrax.ridiculousRPG.ui.StandardMenuService.
 */

/**
 * Called if the MenuService is in this state, and an user input has been
 * performed.
 */
function processInput(keycode, menuService, menu) {
	if (keycode == Keys.P || keycode == Keys.ESCAPE || keycode == Keys.BACK || keycode == Keys.MENU) {
		return menuService.changeState(MENU_STATE_IDLE);
	}
	return false;
}

/**
 * Called if the MenuService switches into this state, to build the gui.
 */
function createGui(menuService, menu) {
	i18nContainer = "engineMenuText";
	var skin = menuService.skinNormal;
	var w = new ui.Window(i18nText("pausemenu.title"), skin);

	var resume = new ui.TextButton(i18nText("pausemenu.resume"), skin);
	resume.addListener(new ui.ClickListener() {
		clicked: function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_IDLE);
		}
	});
	w.row().fill(true, true).expand(true, false);
	w.add(resume);

	var exit = new ui.TextButton(i18nText("pausemenu.return"), skin);
	exit.addListener(new ui.ClickListener() {
		clicked: function (actorEv, x, y) {
			menuService.changeState(MENU_STATE_TITLE);
		}
	});
	w.row().fill(true, true).expand(true, false);
	w.add(exit);

	w.pack();
	menuService.center(w);
	menuService.addGUIcomponent(w);
	menuService.focus(resume);
}
