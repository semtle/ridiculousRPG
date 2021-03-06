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

package com.ridiculousRPG.camera;

import java.io.Serializable;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.ridiculousRPG.GameBase;
import com.ridiculousRPG.service.GameService;

/**
 * @author Alexander Baumgartner
 */
public class CameraZoomService extends InputAdapter implements GameService,
		Serializable {

	private static final long serialVersionUID = 1L;

	private float zoom = 1f;
	private float maxZoomOut = 5f;
	private float minZoomIn = .5f;
	private float zoomInterval = 1.1f;

	@Override
	public boolean keyDown(int keycode) {
		if (GameBase.$().isControlKeyPressed()) {
			if (keycode == Input.Keys.PLUS) {
				zoomIn();
				return true;
			}
			if (keycode == Input.Keys.MINUS) {
				zoomOut();
				return true;
			}
			if (keycode == Input.Keys.NUM_0) {
				zoomNormal();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		if (GameBase.$().isControlKeyPressed()) {
			if (amount > 0)
				zoomIn();
			else
				zoomOut();
			return true;
		}
		return false;
	}

	/**
	 * @return the actual zoom value
	 */
	public float getZoom() {
		return zoom;
	}

	/**
	 * @param zoom
	 *            the new zoom value to set
	 */
	public void setZoom(float zoom) {
		Camera cam = GameBase.$().getCamera();
		float viewportWidth = cam.viewportWidth * zoom / this.zoom;
		float viewportHeight = cam.viewportHeight * zoom / this.zoom;
		float translateX = .5f * (cam.viewportWidth - viewportWidth);
		float translateY = .5f * (cam.viewportHeight - viewportHeight);
		cam.viewportWidth = viewportWidth;
		cam.viewportHeight = viewportHeight;
		cam.translate(translateX, translateY, 0);
		cam.update();
		this.zoom = zoom;
	}

	/**
	 * @return the upper bound for zooming out. (default=5)
	 */
	public float getMaxZoomOut() {
		return maxZoomOut;
	}

	/**
	 * @param maxZoomOut
	 *            the upper bound for zooming out. (default=5)
	 */
	public void setMaxZoomOut(float maxZoomOut) {
		this.maxZoomOut = maxZoomOut;
	}

	/**
	 * @return the lower bound for zooming in. (default=0.5)
	 */
	public float getMinZoomIn() {
		return minZoomIn;
	}

	/**
	 * @param minZoomIn
	 *            the lower bound for zooming in. (default=0.5)
	 */
	public void setMinZoomIn(float minZoomIn) {
		this.minZoomIn = minZoomIn;
	}

	/**
	 * @return the zoom interval used for zooming in/out. (default=1.1)
	 */
	public float getZoomInterval() {
		return zoomInterval;
	}

	/**
	 * This value has to be greater then 1
	 * 
	 * @param zoomInterval
	 *            the zoom interval used for zooming in/out. (default=1.1)
	 */
	public void setZoomInterval(float zoomInterval) {
		if (!(zoomInterval > 1f)) {
			GameBase.$error("Camera.setZoomInterval", "Zoom intervall is "
					+ zoomInterval + " fallback to 1.1",
					new IllegalArgumentException(
							"The zoom interval has to be greater then 1"));
			zoomInterval = 1.1f;
		}
		this.zoomInterval = zoomInterval;
	}

	/**
	 * Zooms the camera to normal zoom value 1
	 */
	public void zoomNormal() {
		zoomBy(0f);
	}

	/**
	 * Zooms the camera by the relative zoom value
	 * 
	 * @see {@link #getZoomInterval()}
	 */
	public void zoomOut() {
		if (zoom < maxZoomOut)
			zoomBy(zoomInterval);
	}

	/**
	 * Zooms the camera by the inverted relative zoom value (1 / zoomInterval)
	 * 
	 * @see {@link #getZoomInterval()}
	 */
	public void zoomIn() {
		if (zoom > minZoomIn)
			zoomBy(1f / zoomInterval);
	}

	/**
	 * Zooms the camera by the given relative amount. An interval of 1 means no
	 * zooming. If the interval is lower or equal zero, the camera will zoom to
	 * normal zoom position (zoom is set to 1).
	 * 
	 * @param interval
	 *            the relative zoom interval
	 */
	public void zoomBy(float interval) {
		if (interval <= 0) {
			setZoom(1f);
		} else {
			Camera cam = GameBase.$().getCamera();
			float viewportWidth = cam.viewportWidth * interval;
			float viewportHeight = cam.viewportHeight * interval;
			float translateX = .5f * (cam.viewportWidth - viewportWidth);
			float translateY = .5f * (cam.viewportHeight - viewportHeight);
			cam.viewportWidth = viewportWidth;
			cam.viewportHeight = viewportHeight;
			cam.translate(translateX, translateY, 0);
			cam.update();
			zoom *= interval;
		}
	}

	public void freeze() {
	}

	public void unfreeze() {
	}

	public boolean essential() {
		return false;
	}

	public void dispose() {
	}
}
