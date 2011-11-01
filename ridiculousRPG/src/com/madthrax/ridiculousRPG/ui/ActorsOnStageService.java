package com.madthrax.ridiculousRPG.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox.ComboBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane.SplitPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.madthrax.ridiculousRPG.GameBase;
import com.madthrax.ridiculousRPG.GameServiceProvider;
import com.madthrax.ridiculousRPG.service.Computable;
import com.madthrax.ridiculousRPG.service.Drawable;
import com.madthrax.ridiculousRPG.service.GameService;
import com.madthrax.ridiculousRPG.service.Initializable;

public class ActorsOnStageService extends Stage implements GameService, Drawable, Computable, Initializable {
	public Skin skinNormal, skinFocused;
	public boolean closeOnAction;

	private boolean awaitingKeyUp;
	private Actor focusedActor = null;
	private static Vector2 tmpPoint = new Vector2(0f, 0f);

	public ActorsOnStageService() {
		super(GameBase.screenWidth, GameBase.screenHeight, true);
		init();
	}
	@Override
	public void init() {
		if (isInitialized() || !GameBase.isGameInitialized()) return;
		setViewport(GameBase.screenWidth, GameBase.screenHeight, true);
		skinNormal = new Skin(Gdx.files.internal("data/uiskin2.json"), Gdx.files.internal("data/uiskin2.png"));
		skinFocused = new Skin(Gdx.files.internal("data/uiskin2.json"), Gdx.files.internal("data/uiskin2Focus.png"));
	    try {
	    	// This codeblock avoids wasting a lot of space in memory.
	    	// It has the same effect as: batch = GameBase.spriteBatch;
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			Field field = Stage.class.getDeclaredField("batch");
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			field.set(this, GameBase.spriteBatch);
		} catch (Exception ignored) {ignored.printStackTrace();}
	}
	@Override
	public boolean isInitialized() {
		return skinNormal!=null;
	}
	@Override
	public void compute(float deltaTime, boolean actionKeyPressed) {
		act(deltaTime);
	}
	@Override
	public void draw(SpriteBatch spriteBatch, Camera camera, boolean debug) {
		getCamera().update();
		super.root.draw(spriteBatch,1f);
	}
	@Override
	public Matrix4 projectionMatrix(Camera camera) {
		return camera.view;
	}
	public static void changeSkin(Actor actor, Skin newSikn) {
		if (actor.getClass() == Button.class) {
			((Button)actor).setStyle(newSikn.getStyle(ButtonStyle.class));
		} else if (actor.getClass() == CheckBox.class
				|| actor.getClass() == com.badlogic.gdx.bugfix.scenes.scene2d.ui.CheckBox.class) {
			((CheckBox)actor).setStyle(newSikn.getStyle(CheckBoxStyle.class));
		} else if (actor.getClass() == TextField.class) {
			((TextField)actor).setStyle(newSikn.getStyle(TextFieldStyle.class));
		} else if (actor.getClass() == ComboBox.class) {
			((ComboBox)actor).setStyle(newSikn.getStyle(ComboBoxStyle.class));
		} else if (actor.getClass() == Slider.class) {
			((Slider)actor).setStyle(newSikn.getStyle(SliderStyle.class));
		} else if (actor.getClass() == SplitPane.class) {
			((SplitPane)actor).setStyle(newSikn.getStyle(SplitPaneStyle.class));
		}
	}
	@Override
	public boolean keyDown(int keycode) {
		// unfocus if actor is removed
		if (focusedActor!=null && !ActorFocusUtil.isActorOnStage(focusedActor, root)) {
			root.keyboardFocus(null);
			focusedActor = null;
			awaitingKeyUp = false;
		}
		// consume tab key down
		if (keycode == Keys.TAB) {
			if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
				return ActorFocusUtil.focusPrev(focusedActor, root, false, false)
					|| ActorFocusUtil.focusLastChild(root);
			}
			return ActorFocusUtil.focusNext(focusedActor, root, false, false)
				|| ActorFocusUtil.focusFirstChild(root);
		}
		// alowed childs to consume key down
		boolean consumed = super.keyDown(keycode);
		if (!consumed && !awaitingKeyUp) {
			switch (keycode) {
			case Keys.SPACE:
			case Keys.ENTER:
				return (awaitingKeyUp = actionKeyPressed(true));
			case Keys.ESCAPE:
				if (focusedActor!=null) {
					focusedActor.parent.keyboardFocus(null);
				}
				return false;
			case Keys.UP:
				return ActorFocusUtil.focusPrev(focusedActor, root, true, false);
			case Keys.DOWN:
				return ActorFocusUtil.focusNext(focusedActor, root, true, false);
			case Keys.LEFT:
				return ActorFocusUtil.focusPrev(focusedActor, root, false, true);
			case Keys.RIGHT:
				return ActorFocusUtil.focusNext(focusedActor, root, false, true);
			}
		}
		return consumed;
	}
	@Override
	public boolean keyUp(int keycode) {
		// unfocus if actor is removed
		if (focusedActor!=null && !ActorFocusUtil.isActorOnStage(focusedActor, root)) {
			root.keyboardFocus(null);
			focusedActor = null;
			awaitingKeyUp = false;
		}
		return checkFocusChanged(keyUpIntern(keycode));
	}
	private boolean checkFocusChanged(boolean consumed) {
		if (focusedActor != root.keyboardFocusedActor) {
			if (focusedActor!=null) changeSkin(focusedActor, skinNormal);
			focusedActor = root.keyboardFocusedActor;
			if (focusedActor!=null) changeSkin(focusedActor, skinFocused);
		}
		return consumed;
	}
	private boolean keyUpIntern(int keycode) {
		if (awaitingKeyUp) {
			switch (keycode) {
			case Keys.SPACE:
			case Keys.ENTER:
				awaitingKeyUp = false;
				actionKeyPressed(false);
				return true;
			}
		}
		return super.keyUp(keycode);
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		boolean consumed = super.touchDown(x, y, pointer, button);
		if (!consumed && !awaitingKeyUp && (pointer==1 || button==Buttons.RIGHT) && focusedActor==null) {
			return (awaitingKeyUp = actionKeyPressed(true));
		}
		return consumed;
	}
	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		return checkFocusChanged(touchUpIntern(x, y, pointer, button));
	}
	private boolean touchUpIntern(int x, int y, int pointer, int button) {
		if (awaitingKeyUp && (pointer==1 || button==Buttons.RIGHT)) {
			awaitingKeyUp = false;
			actionKeyPressed(false);
			return true;
		}
		return super.touchUp(x, y, pointer, button);
	}
	private boolean actionKeyPressed(boolean down) {
		Actor a = focusedActor;
		if (a==null && closeOnAction) {
			System.out.println("Action key pressed - close requested");
		} else if (a!=null) {
			// unfocus if actor is removed
			if (!ActorFocusUtil.isActorOnStage(a, root)) {
				root.keyboardFocus(null);
				focusedActor = null;
				awaitingKeyUp = false;
				return actionKeyPressed(down);
			}
			a.toLocalCoordinates(tmpPoint.set(a.x, a.y));
			// simulate touch event
			if (down) {
				if (GameServiceProvider.requestAttention(this, false, false)) {
					root.touchDown(a.x-tmpPoint.x+1, a.y-tmpPoint.y+1, 0);
					if (a.parent!=null) a.parent.keyboardFocusedActor = a;
					return true;
				}
			} else {
				if (GameServiceProvider.releaseAttention(this)) {
					root.touchUp(a.x-tmpPoint.x+1, a.y-tmpPoint.y+1, 0);
				}
			}
		} else {
			System.out.println("Action key pressed - nothing to do");
		}
		return false;
	}

	@Override
	public void freeze() {}
	@Override
	public void unfreeze() {}
	@Override
	public void dispose() {
		super.dispose();
		skinNormal.dispose();
	}
}
