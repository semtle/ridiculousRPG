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

package com.ridiculousRPG.movement.auto;

import com.ridiculousRPG.event.EventTrigger;
import com.ridiculousRPG.movement.Movable;
import com.ridiculousRPG.movement.MovementHandler;

/**
 * This {@link MovementHandler} tries to move an event to the given position.
 * The move is blocked while a blocking event exists on the given position.<br>
 * After succeeding the switch finished is set to true.<br>
 * If there exists a none-moving blocking event at the given position, this
 * movement will never finish.
 * 
 * @author Alexander Baumgartner
 */
public class MoveSetXYAdapter extends MovementHandler {
	private static final long serialVersionUID = 1L;

	protected boolean checkPerformed;
	public float x, y;

	/**
	 * This MovementAdapter tries to move an event to the given position. The
	 * move is blocked while a blocking event exists on the given position.<br>
	 * After succeeding the switch finished is set to true.<br>
	 * If there exists a none-moving blocking event at the given position, this
	 * movement will never finish.
	 */
	public MoveSetXYAdapter(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * This MovementAdapter tries to move an event to a given other Movable. The
	 * move is blocked forever if two events block mutually.<br>
	 * After succeeding the switch finished is set to true.<br>
	 * If there exists a none-moving blocking event at the given position, this
	 * movement will never finish.
	 */
	public MoveSetXYAdapter(Movable other) {
		this.x = other.getX();
		this.y = other.getY();
	}

	@Override
	public void tryMove(Movable event, float deltaTime,
			EventTrigger eventTrigger) {
		event.stop();
		// move could be blocked
		if (checkPerformed || finished) {
			finished = true;
		} else {
			event.offerMove(x - event.getX(), y - event.getY());
			checkPerformed = true;
		}
	}

	@Override
	public void moveBlocked(Movable event) {
		checkPerformed = false;
	}

	@Override
	public void reset() {
		super.reset();
		checkPerformed = false;
	}

	public void setPosition(Movable event) {
		this.x = event.getX();
		this.y = event.getY();
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
