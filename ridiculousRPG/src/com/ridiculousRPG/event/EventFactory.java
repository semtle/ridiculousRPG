package com.ridiculousRPG.event;

import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.animation.TileAnimation;
import com.ridiculousRPG.event.handler.EventExecScriptAdapter;
import com.ridiculousRPG.event.handler.EventHandler;
import com.ridiculousRPG.movement.MovementHandler;
import com.ridiculousRPG.movement.CombinedMovesAdapter.MoveSegment;
import com.ridiculousRPG.util.BlockingBehavior;
import com.ridiculousRPG.util.Speed;
import com.ridiculousRPG.util.TextureRegionLoader;
import com.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

public class EventFactory {

	private static final char CUSTOM_PROP_KZ = '$';
	// the key is translated to lower case -> we are case insensitive
	private static final String PROP_ID = "id";
	// event will not be created
	private static final String PROP_DISPLAY = "display";
	// event will be created but is hidden
	private static final String PROP_VISIBLE = "visible";
	private static final String PROP_HEIGHT = "height";
	private static final String PROP_OUTREACH = "outreach";
	private static final String PROP_ROTATION = "rotation";
	private static final String PROP_SCALEX = "scalex";
	private static final String PROP_SCALEY = "scaley";
	private static final String PROP_IMAGE = "image";
	private static final String PROP_COLOR = "color";
	private static final String PROP_EFFECT = "effect";
	private static final String PROP_EFFECTFRONT = "effectfront";
	private static final String PROP_EFFECTREAR = "effectrear";
	private static final String PROP_CENTERIMAGE = "centerimage";
	private static final String PROP_BLOCKING = "blocking";
	private static final String PROP_MOVEHANDLER = "movehandler";
	private static final String PROP_MOVEHANDLER_LOOP = "movehandlerloop";
	private static final String PROP_MOVEHANDLER_RESET = "movehandlerreset";
	private static final String PROP_SPEED = "speed";
	private static final String PROP_ANIMATION = "animation";
	private static final String PROP_ESTIMATETOUCHBOUNDS = "estimatetouchbounds";
	private static final String PROP_HANDLER = "eventhandler";
	// the following properties can not be mixed with an eventhandler
	// which doesn't extend the EventExecScriptAdapter
	private static final String PROP_ONPUSH = "onpush";
	private static final String PROP_ONTOUCH = "ontouch";
	private static final String PROP_ONTIMER = "ontimer";
	private static final String PROP_ONCUSTOMEVENT = "oncustomevent";
	private static final String PROP_ONLOAD = "onload";
	// Called if global state changes SEE: GameBase.globalState ObjectState
	private static final String PROP_ONSTATECHANGE = "onstatechange";
	// Polygon object fires an event, when a node is reached
	private static final String PROP_ONNODE = "onnode";

	// To refer to the first/last node independent of the amount of nodes
	private static final String INDEX_LAST = "last";
	private static final String INDEX_FIRST = "first";

	/**
	 * Method to parse the object properties input for an event.
	 * 
	 * @param container
	 * @param props
	 */
	public static void parseProps(EventObject ev, Map<String, String> props) {
		String visibility = null;
		for (Entry<String, String> entry : props.entrySet()) {
			// let's be case insensitive
			String key = entry.getKey().trim().toLowerCase();
			// Fix the behavior of the libgdx XmlReader
			String val = entry.getValue().replace("&quot;", "\"").replace(
					"&gt;", ">").replace("&lt;", "<").replace("&amp;", "&")
					.trim();
			if (key.length() == 0 || val.length() == 0)
				continue;
			if (key.charAt(0) == CUSTOM_PROP_KZ) {
				ev.properties.put(key, val);
			} else if (key.equals(PROP_VISIBLE)) {
				visibility = val;
			} else {
				parseSingleProp(ev, key, val, props);
			}
		}
		if (visibility != null && ev.visible)
			ev.visible = toBool(visibility);
	}

	private static void parseSingleProp(EventObject ev, String key, String val,
			Map<String, String> props) {
		try {
			if (PROP_ID.equals(key)) {
				ev.id = toInt(val);
			} else if (PROP_HEIGHT.equals(key)) {
				ev.z += toInt(val);
			} else if (PROP_BLOCKING.equals(key)) {
				ev.blockingBehavior = BlockingBehavior.parse(val.toUpperCase());
			} else if (PROP_SPEED.equals(key)) {
				ev.setMoveSpeed(Speed.parse(val));
			} else if (PROP_MOVEHANDLER_LOOP.equals(key)) {
				ev.setMoveLoop(toBool(val));
			} else if (PROP_MOVEHANDLER_RESET.equals(key)) {
				ev.setMoveResetEventPosition(toBool(val));
			} else if (key.startsWith(PROP_MOVEHANDLER)) {
				Object evHandler = GameBase.$().eval(val);
				String index = key.substring(PROP_MOVEHANDLER.length()).trim();
				if (evHandler instanceof Class<?>) {
					@SuppressWarnings("unchecked")
					Class<? extends MovementHandler> clazz = (Class<? extends MovementHandler>) evHandler;
					try {
						evHandler = clazz.getMethod("$").invoke(null);
					} catch (NoSuchMethodException e) {
						evHandler = clazz.getConstructor().newInstance();
					}
				}
				if (evHandler instanceof MovementHandler) {
					MovementHandler mv = (MovementHandler) evHandler;
					if (index.length() == 0)
						ev.addMoveSegment(-1, mv);
					else
						ev.addMoveSegment(toInt(index), mv);
				} else if (evHandler instanceof MoveSegment) {
					MoveSegment mv = (MoveSegment) evHandler;
					if (index.length() == 0)
						ev.addMoveSegment(-1, mv);
					else
						ev.addMoveSegment(toInt(index), mv);
				}
			} else if (PROP_OUTREACH.equals(key)) {
				ev.outreach = toInt(val);
			} else if (PROP_COLOR.equals(key)) {
				Object color = GameBase.$().eval(val);
				if (color instanceof Color)
					ev.setColor((Color) color);
			} else if (PROP_ROTATION.equals(key)) {
				ev.rotation = toFloat(val);
			} else if (PROP_SCALEX.equals(key)) {
				ev.scaleX = toFloat(val);
			} else if (PROP_SCALEY.equals(key)) {
				ev.scaleY = toFloat(val);
			} else if (PROP_IMAGE.equals(key)) {
				if (Gdx.files.internal(val).exists()) {
					boolean estimateTouch = toBool(props
							.get(PROP_ESTIMATETOUCHBOUNDS));
					ev.setImage(val, estimateTouch, !estimateTouch);
					if (toBool(props.get(PROP_CENTERIMAGE)))
						ev.centerDrawbound();
				}
			} else if (PROP_EFFECTFRONT.equals(key)) {
				if (Gdx.files.internal(val).exists()) {
					ev.setEffectFront(val);
				}
			} else if (PROP_EFFECT.equals(key)) {
				if (Gdx.files.internal(val).exists()) {
					ev.setEffectFront(val);
					ev.setEffectRear(val);
				}
			} else if (PROP_EFFECTREAR.equals(key)) {
				if (Gdx.files.internal(val).exists()) {
					ev.setEffectRear(val);
				}
			} else if (PROP_ANIMATION.equals(key)) {
				FileHandle fh = Gdx.files.internal(val);
				if (fh.exists()) {
					TextureRegionRef t = TextureRegionLoader.load(val);
					TileAnimation anim = new TileAnimation(val, t
							.getRegionWidth() / 4, t.getRegionHeight() / 4, 4,
							4);
					t.dispose();
					boolean estimateTouch = toBool(props
							.get(PROP_ESTIMATETOUCHBOUNDS));
					ev.setAnimation(anim, estimateTouch, !estimateTouch);
					if (toBool(props.get(PROP_CENTERIMAGE)))
						ev.centerDrawbound();
				} else {
					Object result = GameBase.$().eval(val);
					if (result instanceof TileAnimation) {
						boolean estimateTouch = toBool(props
								.get(PROP_ESTIMATETOUCHBOUNDS));
						ev.setAnimation((TileAnimation) result, estimateTouch,
								!estimateTouch);
						if (toBool(props.get(PROP_CENTERIMAGE)))
							ev.centerDrawbound();
					}
				}
			} else if (PROP_HANDLER.equals(key)) {
				Object evHandler = GameBase.$().eval(val);
				if (evHandler instanceof Class<?>) {
					@SuppressWarnings("unchecked")
					Class<? extends EventHandler> clazz = (Class<? extends EventHandler>) evHandler;
					evHandler = clazz.newInstance();
				}

				// merge both event handler
				if (evHandler instanceof EventExecScriptAdapter
						&& ev.eventHandler instanceof EventExecScriptAdapter) {
					((EventExecScriptAdapter) evHandler)
							.merge((EventExecScriptAdapter) ev.eventHandler);
				} else if (evHandler instanceof EventHandler) {
					ev.eventHandler = (EventHandler) evHandler;
				}
			} else if (key.startsWith(PROP_ONPUSH)) {
				ev.pushable = true;
				if (ev.eventHandler == null) {
					ev.eventHandler = new EventExecScriptAdapter(ev);
				}
				if (ev.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONPUSH.length()).trim();
					((EventExecScriptAdapter) ev.eventHandler).execOnPush(val,
							index.length() == 0 ? -1 : toInt(index));
				}
			} else if (key.startsWith(PROP_ONSTATECHANGE)) {
				if (ev.eventHandler == null) {
					ev.eventHandler = new EventExecScriptAdapter(ev);
				}
				if (ev.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONSTATECHANGE.length())
							.trim();
					((EventExecScriptAdapter) ev.eventHandler)
							.execOnStateChange(val, index.length() == 0 ? -1
									: toInt(index));
				}
			} else if (key.startsWith(PROP_ONTOUCH)) {
				ev.touchable = true;
				if (ev.eventHandler == null) {
					ev.eventHandler = new EventExecScriptAdapter(ev);
				}
				if (ev.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONTOUCH.length()).trim();
					((EventExecScriptAdapter) ev.eventHandler).execOnTouch(val,
							index.length() == 0 ? -1 : toInt(index));
				}
			} else if (key.startsWith(PROP_ONTIMER)) {
				if (ev.eventHandler == null) {
					ev.eventHandler = new EventExecScriptAdapter(ev);
				}
				if (ev.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONTIMER.length()).trim();
					((EventExecScriptAdapter) ev.eventHandler).execOnTimer(val,
							index.length() == 0 ? -1 : toInt(index));
				}
			} else if (key.startsWith(PROP_ONCUSTOMEVENT)) {
				if (ev.eventHandler == null) {
					ev.eventHandler = new EventExecScriptAdapter(ev);
				}
				if (ev.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONCUSTOMEVENT.length())
							.trim();
					((EventExecScriptAdapter) ev.eventHandler)
							.execOnCustomTrigger(val, index.length() == 0 ? -1
									: toInt(index));
				}
			} else if (key.startsWith(PROP_ONLOAD)) {
				if (ev.eventHandler == null) {
					ev.eventHandler = new EventExecScriptAdapter(ev);
				}
				if (ev.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONLOAD.length()).trim();
					((EventExecScriptAdapter) ev.eventHandler).execOnLoad(val,
							index.length() == 0 ? -1 : toInt(index));
				}
			}
		} catch (Exception e) {
			GameBase.$error("TiledMap.createEvent",
					"Could not parse property '" + key + "' for event '"
							+ ev.name + "'", e);
		}
	}

	/**
	 * Method to parse the object properties input for a polygon.
	 * 
	 * @param container
	 * @param props
	 */
	public static void parseProps(PolygonObject poly, Map<String, String> props) {
		String visibility = null;
		for (Entry<String, String> entry : props.entrySet()) {
			// let's be case insensitive
			String key = entry.getKey().trim().toLowerCase();
			// Fix the behavior of the libgdx XmlReader
			String val = entry.getValue().replace("&quot;", "\"").replace(
					"&gt;", ">").replace("&lt;", "<").replace("&amp;", "&")
					.trim();
			if (key.length() == 0 || val.length() == 0)
				continue;
			if (key.charAt(0) == CUSTOM_PROP_KZ) {
				poly.properties.put(key, val);
			} else if (key.equals(PROP_VISIBLE)) {
				visibility = val;
			} else {
				parseSingleProp(poly, key, val, props);
			}
		}
		if (visibility != null && poly.visible)
			poly.visible = toBool(visibility);
	}

	private static void parseSingleProp(PolygonObject poly, String key,
			String val, Map<String, String> props) {
		// let's be case insensitive
		key = key.toLowerCase();
		try {
			if (key.startsWith(PROP_ONNODE)) {
				String suffix = key.substring(PROP_ONNODE.length()).trim()
						.toLowerCase();
				int index;
				if (suffix.equals(INDEX_FIRST)) {
					index = 0;
				} else if (suffix.equals(INDEX_LAST)) {
					index = poly.execAtNodeScript.length - 1;
				} else {
					index = toInt(suffix);
				}
				if (index >= poly.execAtNodeScript.length) {
					GameBase.$info("TiledMap.createPolygon",
							"Could not apply event '" + key + "' for polygon '"
									+ poly.getName() + "'. "
									+ "Node index out of bounds!", null);
				} else {
					poly.execAtNodeScript[index] = val;
				}
			} else if (PROP_BLOCKING.equals(key)) {
				poly.blockingBehavior = BlockingBehavior.parse(val
						.toUpperCase());
			} else if (PROP_COLOR.equals(key)) {
				Object color = GameBase.$().eval(val);
				if (color instanceof Color)
					poly.setColor((Color) color);
			} else if (PROP_HANDLER.equals(key)) {
				Object evHandler = GameBase.$().eval(val);
				if (evHandler instanceof Class<?>) {
					@SuppressWarnings("unchecked")
					Class<? extends EventHandler> clazz = (Class<? extends EventHandler>) evHandler;
					evHandler = clazz.newInstance();
				}

				// merge both event handler
				if (evHandler instanceof EventExecScriptAdapter
						&& poly.eventHandler instanceof EventExecScriptAdapter) {
					((EventExecScriptAdapter) evHandler)
							.merge((EventExecScriptAdapter) poly.eventHandler);
				} else if (evHandler instanceof EventHandler) {
					poly.eventHandler = (EventHandler) evHandler;
				}
			} else if (key.startsWith(PROP_ONSTATECHANGE)) {
				if (poly.eventHandler == null) {
					poly.eventHandler = new EventExecScriptAdapter(poly);
				}
				if (poly.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONSTATECHANGE.length())
							.trim();
					((EventExecScriptAdapter) poly.eventHandler)
							.execOnStateChange(val, index.length() == 0 ? -1
									: toInt(index));
				}
			} else if (key.startsWith(PROP_ONPUSH)) {
				GameBase.$error("Polygon.onpush", "The event onpush is not "
						+ "available for polygon objects (for perfornamce "
						+ "reasons)", new IllegalArgumentException("Argument "
						+ key + " invalid"));
			} else if (key.startsWith(PROP_ONTOUCH)) {
				poly.touchable = true;
				if (poly.eventHandler == null) {
					poly.eventHandler = new EventExecScriptAdapter(poly);
				}
				if (poly.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONTOUCH.length()).trim();
					((EventExecScriptAdapter) poly.eventHandler).execOnTouch(
							val, index.length() == 0 ? -1 : toInt(index));
				}
			} else if (key.startsWith(PROP_ONTIMER)) {
				if (poly.eventHandler == null) {
					poly.eventHandler = new EventExecScriptAdapter(poly);
				}
				if (poly.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONTIMER.length()).trim();
					((EventExecScriptAdapter) poly.eventHandler).execOnTimer(
							val, index.length() == 0 ? -1 : toInt(index));
				}
			} else if (key.startsWith(PROP_ONCUSTOMEVENT)) {
				if (poly.eventHandler == null) {
					poly.eventHandler = new EventExecScriptAdapter(poly);
				}
				if (poly.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONCUSTOMEVENT.length())
							.trim();
					((EventExecScriptAdapter) poly.eventHandler)
							.execOnCustomTrigger(val, index.length() == 0 ? -1
									: toInt(index));
				}
			} else if (key.startsWith(PROP_ONLOAD)) {
				if (poly.eventHandler == null) {
					poly.eventHandler = new EventExecScriptAdapter(poly);
				}
				if (poly.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONLOAD.length()).trim();
					((EventExecScriptAdapter) poly.eventHandler).execOnLoad(
							val, index.length() == 0 ? -1 : toInt(index));
				}
			}
		} catch (Exception e) {
			GameBase.$error("TiledMap.createPolygon",
					"Could not parse property '" + key + "' for polygon '"
							+ poly.getName() + "'", e);
		}
	}

	/**
	 * Method to parse the object properties input for a ellipse.
	 * 
	 * @param container
	 * @param props
	 */
	public static void parseProps(EllipseObject ell, Map<String, String> props) {
		String visibility = null;
		for (Entry<String, String> entry : props.entrySet()) {
			// let's be case insensitive
			String key = entry.getKey().trim().toLowerCase();
			// Fix the behavior of the libgdx XmlReader
			String val = entry.getValue().replace("&quot;", "\"").replace(
					"&gt;", ">").replace("&lt;", "<").replace("&amp;", "&")
					.trim();
			if (key.length() == 0 || val.length() == 0)
				continue;
			if (key.charAt(0) == CUSTOM_PROP_KZ) {
				ell.properties.put(key, val);
			} else if (key.equals(PROP_VISIBLE)) {
				visibility = val;
			} else {
				parseSingleProp(ell, key, val, props);
			}
		}
		if (visibility != null && ell.visible)
			ell.visible = toBool(visibility);
	}

	private static void parseSingleProp(EllipseObject ell, String key,
			String val, Map<String, String> props) {
		// let's be case insensitive
		key = key.toLowerCase();
		try {
			if (PROP_BLOCKING.equals(key)) {
				ell.blockingBehavior = BlockingBehavior
						.parse(val.toUpperCase());
			} else if (PROP_COLOR.equals(key)) {
				Object color = GameBase.$().eval(val);
				if (color instanceof Color)
					ell.setColor((Color) color);
			} else if (PROP_HANDLER.equals(key)) {
				Object evHandler = GameBase.$().eval(val);
				if (evHandler instanceof Class<?>) {
					@SuppressWarnings("unchecked")
					Class<? extends EventHandler> clazz = (Class<? extends EventHandler>) evHandler;
					evHandler = clazz.newInstance();
				}

				// merge both event handler
				if (evHandler instanceof EventExecScriptAdapter
						&& ell.eventHandler instanceof EventExecScriptAdapter) {
					((EventExecScriptAdapter) evHandler)
							.merge((EventExecScriptAdapter) ell.eventHandler);
				} else if (evHandler instanceof EventHandler) {
					ell.eventHandler = (EventHandler) evHandler;
				}
			} else if (key.startsWith(PROP_ONSTATECHANGE)) {
				if (ell.eventHandler == null) {
					ell.eventHandler = new EventExecScriptAdapter(ell);
				}
				if (ell.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONSTATECHANGE.length())
							.trim();
					((EventExecScriptAdapter) ell.eventHandler)
							.execOnStateChange(val, index.length() == 0 ? -1
									: toInt(index));
				}
			} else if (key.startsWith(PROP_ONPUSH)) {
				GameBase.$error("Ellipse.onpush", "The event onpush is not "
						+ "available for ellipse objects (for perfornamce "
						+ "reasons)", new IllegalArgumentException("Argument "
						+ key + " invalid"));
			} else if (key.startsWith(PROP_ONTOUCH)) {
				ell.touchable = true;
				if (ell.eventHandler == null) {
					ell.eventHandler = new EventExecScriptAdapter(ell);
				}
				if (ell.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONTOUCH.length()).trim();
					((EventExecScriptAdapter) ell.eventHandler).execOnTouch(
							val, index.length() == 0 ? -1 : toInt(index));
				}
			} else if (key.startsWith(PROP_ONTIMER)) {
				if (ell.eventHandler == null) {
					ell.eventHandler = new EventExecScriptAdapter(ell);
				}
				if (ell.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONTIMER.length()).trim();
					((EventExecScriptAdapter) ell.eventHandler).execOnTimer(
							val, index.length() == 0 ? -1 : toInt(index));
				}
			} else if (key.startsWith(PROP_ONCUSTOMEVENT)) {
				if (ell.eventHandler == null) {
					ell.eventHandler = new EventExecScriptAdapter(ell);
				}
				if (ell.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONCUSTOMEVENT.length())
							.trim();
					((EventExecScriptAdapter) ell.eventHandler)
							.execOnCustomTrigger(val, index.length() == 0 ? -1
									: toInt(index));
				}
			} else if (key.startsWith(PROP_ONLOAD)) {
				if (ell.eventHandler == null) {
					ell.eventHandler = new EventExecScriptAdapter(ell);
				}
				if (ell.eventHandler instanceof EventExecScriptAdapter) {
					String index = key.substring(PROP_ONLOAD.length()).trim();
					((EventExecScriptAdapter) ell.eventHandler).execOnLoad(val,
							index.length() == 0 ? -1 : toInt(index));
				}
			}
		} catch (Exception e) {
			GameBase.$error("TiledMap.createEllipse",
					"Could not parse property '" + key + "' for ellipse '"
							+ ell.getName() + "'", e);
		}
	}

	public static int getZIndex(Map<String, String> properties) {
		return toInt(properties.get(PROP_HEIGHT));
	}

	public static int getZIndex(TiledMap map, int tile) {
		return toInt(map.getTileProperty(tile, PROP_HEIGHT));
	}

	private static boolean toBool(String p) {
		if (p == null)
			return false;
		p = p.trim();
		if (p.length() == 0)
			return false;
		char c = Character.toLowerCase(p.charAt(0));
		// true / yes / 1
		return c == 't' || c == 'y' || c == '1';
	}

	private static boolean toBoolNot(String p) {
		if (p == null)
			return false;
		p = p.trim();
		if (p.length() == 0)
			return false;
		char c = Character.toLowerCase(p.charAt(0));
		// false / no / 0
		return c == 'f' || c == 'n' || c == '0';
	}

	public static boolean isSkip(Map<String, String> properties) {
		return toBoolNot(properties.get(PROP_DISPLAY));
	}

	private static int toInt(String prop) {
		if (prop != null && prop.length() > 0) {
			try {
				return Integer.parseInt(prop);
			} catch (NumberFormatException e) {
			}
		}
		return 0;
	}

	private static float toFloat(String prop) {
		if (prop != null && prop.length() > 0) {
			try {
				return Float.parseFloat(prop);
			} catch (NumberFormatException e) {
			}
		}
		return 0;
	}
}
