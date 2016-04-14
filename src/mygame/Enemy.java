package mygame;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;

public abstract class Enemy {

    Main main;
    Geometry collisionTest;
    Node enemyNode, bulletStartNode, walkDirNode, leftNode, leftNode1, rightNode, rightNode1;
    CharacterControl enemyControl;
    List<Bullet> bulletList;
    Dust dust;
    Vector3f walkDirection = new Vector3f(0, 0, 0), viewDirection = new Vector3f(0, 0, 0);
    boolean force = false, second = false, forward = false, backward = false,
            leftRotate = false, rightRotate = false, attack = false, shoot = false;
    protected float airTime = 0;

    public Enemy(Main main, String enemyType, Material mat) {
        this.main = main;
        initEnemy(enemyType, mat);
    }

    private void initEnemy(String enemyType, Material mat) {
        Sphere sphereLarge = new Sphere(32, 32, 1.5f);
        collisionTest = new Geometry("Shiny", sphereLarge);
        Material mat1 = new Material(main.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        collisionTest.setMaterial(mat1);
        bulletList = new ArrayList<Bullet>();
        SphereCollisionShape sphere = new SphereCollisionShape(5f);
        enemyControl = new CharacterControl(sphere, 0.01f);
        enemyControl.setFallSpeed(15f);
        enemyControl.setGravity(30f);
        enemyNode = (Node) main.getAssetManager().loadModel(enemyType);
        enemyNode.setMaterial(mat);
        enemyNode.addControl(enemyControl);
        enemyNode.attachChild(collisionTest);

        enemyNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        bulletStartNode = new Node();
        bulletStartNode.setLocalTranslation(0, 2, 3);
        enemyNode.attachChild(bulletStartNode);

        leftNode = new Node();
        leftNode.setLocalTranslation(0.02f, 0, 6);
        enemyNode.attachChild(leftNode);

        leftNode1 = new Node();
        leftNode1.setLocalTranslation(0.08f, 0, 6);
        enemyNode.attachChild(leftNode1);

        rightNode = new Node();
        rightNode.setLocalTranslation(-0.02f, 0, 6);
        enemyNode.attachChild(rightNode);

        rightNode1 = new Node();
        rightNode1.setLocalTranslation(-0.08f, 0, 6);
        enemyNode.attachChild(rightNode1);

        dust = new Dust(main);
        dust.emit.setParticlesPerSec(0f);
        enemyNode.attachChild(dust.emit);
        //initPhysics();
    }

    private void initPhysics() {
        RigidBodyControl phyEnemy = new RigidBodyControl(0.0f);
        collisionTest.addControl(phyEnemy);
        //phyEnemy.setKinematic(true);
        main.bulletAppState.getPhysicsSpace().add(phyEnemy);
        //geomMarble.getUserData(null).
    }

    protected abstract void adjust(Vector3f palyerPos);

    protected abstract void updateEnemy(float tpf, Vector3f playerPos);
}
