// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.github.stateless4j.sample;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;

/*
 * @startuml
 * IDLE: entry: startBlinking()\nexit:stopBlinking()
 * [*] -> IDLE
 * IDLE -> SELECTION: COIN_INSERTED
 * SELECTION : entry: showList()\nexit: hideList()
 * SELECTION -> IDLE: CANCEL\nreturnCoin()
 * SELECTION -d-> RUNNING: START\nconsumeCoin()
 * state RUNNING {
 *      [*] -> PLAYING
 *      PLAYING -> PAUSED: PAUSE
 *      PAUSED -> PLAYING: RESUME
 *      PLAYING : entry: playSong()
 *      PAUSED : entry: pauseSong()
 * }
 * RUNNING : entry: showTimer()\nexit: hideTimer()
 * RUNNING -> IDLE: STOP
 * @enduml
 */
public class Main {

    private enum State {
        IDLE,
        SELECTION,
        RUNNING,
        PLAYING,
        PAUSED
    }

    private enum Trigger {
        COIN_INSERTED,
        CANCEL,
        STOP,
        START,
        PAUSE,
        RESUME
    }

    public static void main(String[] args) {
        var config = new StateMachineConfig<State, Trigger>();

        config.configure(State.IDLE)
                .onEntry(Main::startBlinking)
                .onExit(Main::stopBlinking)
                .permit(Trigger.COIN_INSERTED, State.SELECTION);

        config.configure(State.SELECTION)
                .onEntry(Main::showSongsList)
                .onExit(Main::hideSongsList)
                .permit(Trigger.START, State.PLAYING, Main::consumeCoin)
                .permit(Trigger.CANCEL, State.IDLE, Main::returnCoin);

        config.configure(State.RUNNING)
                .onEntry(Main::showTimer)
                .onExit(Main::hideTimer)
                .permit(Trigger.STOP, State.IDLE);

        config.configure(State.PLAYING)
                .substateOf(State.RUNNING)
                .onEntry(Main::playSong)
                .permit(Trigger.PAUSE, State.PAUSED);

        config.configure(State.PAUSED)
                .substateOf(State.RUNNING)
                .onEntry(Main::pauseSong)
                .permit(Trigger.RESUME, State.PLAYING);

        var fsm = new StateMachine<>(State.IDLE, config);
        fsm.fireInitialTransition();
        fsm.fire(Trigger.COIN_INSERTED);
        fsm.fire(Trigger.CANCEL);
        fsm.fire(Trigger.COIN_INSERTED);
        fsm.fire(Trigger.START);
        fsm.fire(Trigger.PAUSE);
        fsm.fire(Trigger.RESUME);
        fsm.fire(Trigger.STOP);
    }

    private static void showSongsList() {
        System.out.println("Showing song list:\n\t1. La la la\n\t2. Guli guli guli\n\t3. A Ram Sam Sam");
    }

    private static void hideSongsList() {
        System.out.println("Hiding song list");
    }

    private static void consumeCoin() {
        System.out.println("Coin consumed");
    }

    private static void returnCoin() {
        System.out.println("Coin returned");
    }

    private static void showTimer() {
        System.out.println("Showing song timer");
    }

    private static void hideTimer() {
        System.out.println("Hiding song timer");
    }

    private static void playSong() {
        System.out.println("Playing selected song...");
    }

    private static void pauseSong() {
        System.out.println("Pausing...");
    }

    private static void startBlinking() {
        System.out.println("Start blinking: blink, blonk, blink, blonk...");
    }

    private static void stopBlinking() {
        System.out.println("Stop blinking");
    }
}
