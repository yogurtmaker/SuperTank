/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alien
 */
public abstract class Enemy {

    Node enemyNode;
    Shield shield;
    boolean force = false;
    boolean second = false;
    Vector3f walkDirection = new Vector3f(0, 0, 0);
    Vector3f viewDirection = new Vector3f(0, 0, 0);
    boolean forward = false, backward = false, leftRotate = false, rightRotate = false, attack = false, shoot = false;
    protected float airTime = 0;
    CharacterControl enemyControl;
    Node bulletStartNode, leftNode, leftNode1, rightNode, rightNode1;
    Node walkDirNode;
    Main main;
    Dust dust;
    List<Bullet> bulletList;

    public Enemy(Main main, String enemyType) {
        this.main = main;
        initEnemy(enemyType);
    }

    private void initEnemy(String enemyType) {
        bulletList = new ArrayList<Bullet>();
        SphereCollisionShape sphere = new SphereCollisionShape(5f);
        enemyControl = new CharacterControl(sphere, 0.01f);
        enemyControl.setFallSpeed(15f);
        enemyControl.setGravity(30f);
        enemyNode = (Node) main.getAssetManager().loadModel(enemyType);
        enemyNode.addControl(enemyControl);

        enemyNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        bulletStartNode = new Node();
        bulletStartNode.setLocalTranslation(0, 3, 6);
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
        
        shield = new Shield(main);
        dust = new Dust(main);
        dust.emit.setParticlesPerSec(0f);
        enemyNode.attachChild(dust.emit);
    }

    // -------------------------------------------------------------------------
    // Abstract Methods
    // adjustment: sets the specific local translation and scaling for a certain
    // mesh
    protected abstract void adjust(Vector3f palyerPos);

    protected abstract void updateEnemy(float tpf, Vector3f playerPos);
}
