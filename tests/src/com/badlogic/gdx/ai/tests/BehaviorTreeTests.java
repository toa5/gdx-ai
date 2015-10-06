/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.tests.btree.BehaviorTreeTestBase;
import com.badlogic.gdx.ai.tests.btree.tests.IncludeSubtreeTest;
import com.badlogic.gdx.ai.tests.btree.tests.ParseAndRunTest;
import com.badlogic.gdx.ai.tests.btree.tests.ParseCloneAndRunTest;
import com.badlogic.gdx.ai.tests.btree.tests.ProgrammaticallyCreatedTest;
import com.badlogic.gdx.ai.tests.btree.tests.SemaphoreGuardTest;
import com.badlogic.gdx.ai.tests.utils.GdxAiTest;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** Test class for behavior trees.
 * 
 * @author davebaol */
public class BehaviorTreeTests extends GdxAiTest {

	public static void main (String[] argv) {
		launch(new BehaviorTreeTests());
	}

	private static final boolean DEBUG_STAGE = false;

	private static String LABEL_FPS = "FPS: ";

	private Label fpsLabel;
	private int fps = 0;
	private Label testDescriptionLabel;

	// @off - disable libgdx formatter
	BehaviorTreeTestBase[] tests = {
		new ParseAndRunTest(),
		new ParseCloneAndRunTest(),
		new IncludeSubtreeTest(false),
		new IncludeSubtreeTest(true),
		new ProgrammaticallyCreatedTest(false),
		new ProgrammaticallyCreatedTest(true),
		new SemaphoreGuardTest()
	};
	// @on - enable libgdx formatter

	BehaviorTreeTestBase currentTest;

	Table testTable;
	SplitPane splitPane;

	public Stage stage;
	public float stageWidth;
	public float stageHeight;
	public Skin skin;
	
	@Override
	public void create () {
		Gdx.gl.glClearColor(.3f, .3f, .3f, 1);

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		stage = new Stage(new ScreenViewport());
		stage.setDebugAll(DEBUG_STAGE);
		stageWidth = stage.getWidth();
		stageHeight = stage.getHeight();

		Gdx.input.setInputProcessor(stage);

		// Create split pane
		ScrollPane leftScrollPane = new ScrollPane(createTestList(), skin);
		testTable = new Table(skin);
		ScrollPane rigthScrollPane = new ScrollPane(testTable, skin);
		splitPane = new SplitPane(leftScrollPane, rigthScrollPane, false, skin, "default-horizontal");

		Table t = new Table(skin);
		t.setFillParent(true);
		t.add(splitPane).colspan(2).grow();
		t.row();
		t.add(fpsLabel = new Label(LABEL_FPS + fps, skin)).left();
		t.add(testDescriptionLabel = new Label("", skin)).center();
		stage.addActor(t);

		// Set selected test
		changeTest(0);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (fps != Gdx.graphics.getFramesPerSecond()) {
			fps = Gdx.graphics.getFramesPerSecond();
			StringBuilder sb = fpsLabel.getText();
			sb.setLength(LABEL_FPS.length());
			sb.append(fps);
			fpsLabel.invalidateHierarchy();
		}

		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height, true);
		stageWidth = width;
		stageHeight = height;
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}

	private List<String> createTestList () {
		// Create behavior names
		int numTests = tests.length;
		String[] testNames = new String[numTests];
		for (int i = 0; i < numTests; i++) {
			testNames[i] = tests[i].testName;
		}

		final List<String> testList = new List<String>(skin);
		testList.setItems(testNames);
		testList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				changeTest(testList.getSelectedIndex());
			}
		});
		return testList;
	}

	private void changeTest (int testIndex) {
		// Remove and dispose the previous test
		testTable.clear();
		if (currentTest != null) currentTest.dispose();

		// Add the new test
		currentTest = tests[testIndex];
		Gdx.app.log("BehaviorTreeTests", "***********************************************");
		Gdx.app.log("BehaviorTreeTests", "Starting test " + currentTest.getClass().getSimpleName());
		Gdx.app.log("BehaviorTreeTests", "***********************************************");
		String description = currentTest.getDescription();
		if (description != null) {
			Gdx.app.log("BehaviorTreeTests", description);
			Gdx.app.log("BehaviorTreeTests", "***********************************************");
			testDescriptionLabel.setText(description);
		}
		else {
			testDescriptionLabel.setText("Look at the log and see what's happening");
		}
		testTable.add(currentTest.createActor(skin)).grow();
		splitPane.invalidate();
	}

}
