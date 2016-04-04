package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl;
import com.jme3.system.AppSettings;
import java.awt.Dimension;
import java.awt.Toolkit;

public class Main extends SimpleApplication {

    static Dimension screen;
    BulletAppState bulletAppState;
    private Node modelPlayer;
    private Node[] modelEnemyTank;
    private CharacterControl player;
    private CharacterControl[] controlEnemyTank;
    CameraNode camNode;
    Ground ground;
    Sky sky;
    Tank tank;
    Enemy[] enemyTank;
    final int ENEMYNUMBER = 4;
    boolean rotate = false;

    public static void main(final String[] args) {
        Main app = new Main();
        initAppScreen(app);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        processor();
        ground = new Ground(this);
        sky = new Sky(this);
        initPhysics();
        createCharacter();
        initCam();
        setUpKeys();
    }

    private static void initAppScreen(SimpleApplication app) {
        AppSettings aps = new AppSettings(true);
        screen = Toolkit.getDefaultToolkit().getScreenSize();
        screen.width *= 0.75;
        screen.height *= 0.75;
        aps.setResolution(screen.width, screen.height);
        app.setSettings(aps);
        app.setShowSettings(false);
    }

    @Override
    public void simpleUpdate(final float tpf) {
        tank.updateTank(tpf);
        sky.skyUpdate(tpf);
        for (int i = 0; i < ENEMYNUMBER; i++) {
            enemyTank[i].updateEnemy(tpf, tank.tankNode.getWorldTranslation());
        }
    }

    private void createCharacter() {
        tank = new Tank(this);
        modelPlayer = tank.tankNode;
        player = tank.tankControl;
        rootNode.attachChild(modelPlayer);
        createEnemy();
        bulletAppState.getPhysicsSpace().add(player);
    }

    private void initPhysics() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
    }

    private void createEnemy() {
        enemyTank = new EnemyTank[ENEMYNUMBER];
        modelEnemyTank = new Node[ENEMYNUMBER];
        controlEnemyTank = new CharacterControl[ENEMYNUMBER];
        for (int i = 0; i < ENEMYNUMBER; i++) {
            enemyTank[i] = new EnemyTank(this);
            modelEnemyTank[i] = new Node();
            modelEnemyTank[i] = enemyTank[i].enemyNode;
            enemyTank[i].adjust(tank.tankNode.getWorldTranslation());
            controlEnemyTank[i] = enemyTank[i].enemyControl;
            bulletAppState.getPhysicsSpace().add(controlEnemyTank[i]);
            System.out.println(enemyTank[i].enemyNode.getWorldTranslation());
            rootNode.attachChild(modelEnemyTank[i]);
        }
    }

    private void setUpKeys() {
        inputManager.addMapping("Rotate Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Rotate Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Walk Forward", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Walk Backward", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Shot", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Shield", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(tank, "Rotate Left", "Rotate Right");
        inputManager.addListener(tank, "Walk Forward", "Walk Backward");
        inputManager.addListener(tank, "Shot");
        inputManager.addListener(tank, "Shield");
    }

    private void initCam() {
        flyCam.setEnabled(false);
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0, 5, -25));
        camNode.lookAt(tank.tankNode.getLocalTranslation(), Vector3f.UNIT_Y);
        tank.tankNode.attachChild(camNode);
    }

    private void processor() {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        int numSamples = getContext().getSettings().getSamples();
        if (numSamples > 0) {
            fpp.setNumSamples(numSamples);
        }
        FogFilter fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(165f, 145f, 121f, 1.0f));
        fog.setFogDistance(2000);
        fog.setFogDensity(0.00425f);
        fpp.addFilter(fog);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        bloom.setBloomIntensity(2.5f);
        bloom.setBlurScale(2.5f);
        bloom.setExposurePower(1f);
        fpp.addFilter(bloom);
        viewPort.addProcessor(fpp);
    }
}