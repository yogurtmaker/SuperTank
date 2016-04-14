package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
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
    Material mats[];

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
        initMat();
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
            if (enemyTank[i].shoot = false) {
                System.out.println("123");
            }
        }
        collisionTest();
        for (int i = 0; i < ENEMYNUMBER; i++) {
            for (int j = 0; j < enemyTank[i].bulletList.size(); j++) {
                if (enemyTank[i].bulletList.get(j).bullet.getWorldTranslation().subtract(tank.tankNode.getWorldTranslation()).length()
                        > 2000) {
                    rootNode.detachChild(enemyTank[i].bulletList.get(j).bullet);
                    enemyTank[i].bulletList.remove(enemyTank[i].bulletList.get(j));
                }
            }
        }

        for (int j = 0; j < tank.bulletList.size(); j++) {
            if (tank.bulletList.get(j).bullet.getWorldTranslation().subtract(tank.tankNode.getWorldTranslation()).length()
                    > 2000) {
                rootNode.detachChild(tank.bulletList.get(j).bullet);
                tank.bulletList.remove(tank.bulletList.get(j));
            }
        }

    }

    public void collisionTest() {
        CollisionResults crs = new CollisionResults();
        for (int i = 0; i < ENEMYNUMBER; i++) {
            for (int j = 0; j < enemyTank[i].bulletList.size(); j++) {
                BoundingVolume bv = enemyTank[i].bulletList.get(j).bullet.getWorldBound();
                tank.shield.nodeshield.collideWith(bv, crs);
                if (crs.size() > 0) {
                    if (crs.getClosestCollision() != null) {
                        tank.shield.forceShieldControl.registerHit(enemyTank[i].bulletList.get(j).bullet.getWorldTranslation());
                    }
                    rootNode.detachChild(enemyTank[i].bulletList.get(j).bullet);
                    enemyTank[i].bulletList.remove(enemyTank[i].bulletList.get(j));
                    crs.clear();
                } else {
                    tank.tankNode.collideWith(bv, crs);
                    if (crs.size() > 0) {
                        System.out.println("Hit player!");
                        rootNode.detachChild(enemyTank[i].bulletList.get(j).bullet);
                        enemyTank[i].bulletList.remove(enemyTank[i].bulletList.get(j));
                        crs.clear();
                    }
                }
            }
        }

        for (int i = 0; i < ENEMYNUMBER; i++) {
            for (int j = 0; j < tank.bulletList.size(); j++) {
                BoundingVolume bv = tank.bulletList.get(j).bullet.getWorldBound();
                enemyTank[i].enemyNode.collideWith(bv, crs);
                if (crs.size() > 0) {
                    System.out.println("Hit enemy!");
                    rootNode.detachChild(tank.bulletList.get(j).bullet);
                    tank.bulletList.remove(tank.bulletList.get(j));
                    crs.clear();
                }
            }
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
            enemyTank[i] = new EnemyTank(this, mats[i]);
            modelEnemyTank[i] = new Node();
            modelEnemyTank[i] = enemyTank[i].enemyNode;
            enemyTank[i].adjust(tank.tankNode.getWorldTranslation());
            controlEnemyTank[i] = enemyTank[i].enemyControl;
            bulletAppState.getPhysicsSpace().add(controlEnemyTank[i]);
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

    public void initMat() {
        mats = new Material[4];
        mats[0] = assetManager
                .loadMaterial("Materials/Active/MultiplyColor_Base.j3m");
        mats[1] = assetManager
                .loadMaterial("Materials/Active/MultiplyColor_2.j3m");
        mats[2] = assetManager
                .loadMaterial("Materials/Active/MultiplyColor_2.j3m");
        mats[3] = assetManager
                .loadMaterial("Materials/Active/MultiplyColor_Base.j3m");
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