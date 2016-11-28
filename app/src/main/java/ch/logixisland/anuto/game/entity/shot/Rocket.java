package ch.logixisland.anuto.game.entity.shot;

import android.graphics.Canvas;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.game.GameEngine;
import ch.logixisland.anuto.game.entity.Entity;
import ch.logixisland.anuto.game.entity.effect.Explosion;
import ch.logixisland.anuto.game.render.Layers;
import ch.logixisland.anuto.game.render.Drawable;
import ch.logixisland.anuto.game.entity.enemy.Enemy;
import ch.logixisland.anuto.game.render.Sprite;
import ch.logixisland.anuto.util.Random;
import ch.logixisland.anuto.util.math.vector.Vector2;

public class Rocket extends HomingShot {

    private final static float MOVEMENT_SPEED = 2.5f;
    private final static float ANIMATION_SPEED = 3f;

    private class StaticData {
        public Sprite sprite;
        public Sprite spriteFire;
    }

    private float mDamage;
    private float mRadius;
    private float mAngle;

    private Sprite.FixedInstance mSprite;
    private Sprite.AnimatedInstance mSpriteFire;

    public Rocket(Entity origin, Vector2 position, float damage, float radius) {
        super(origin);
        setPosition(position);
        setSpeed(MOVEMENT_SPEED);
        setEnabled(false);

        mDamage = damage;
        mRadius = radius;

        StaticData s = (StaticData)getStaticData();

        mSprite = s.sprite.yieldStatic(Layers.SHOT);
        mSprite.setListener(this);
        mSprite.setIndex(Random.next(4));

        mSpriteFire = s.spriteFire.yieldAnimated(Layers.SHOT);
        mSpriteFire.setListener(this);
        mSpriteFire.setSequence(mSpriteFire.sequenceForward());
        mSpriteFire.setFrequency(ANIMATION_SPEED);
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.sprite = Sprite.fromResources(R.drawable.rocket, 4);
        s.sprite.setMatrix(0.8f, 1f, null, -90f);

        s.spriteFire = Sprite.fromResources(R.drawable.rocket_fire, 4);
        s.spriteFire.setMatrix(0.3f, 0.3f, new Vector2(0.15f, 0.6f), -90f);

        return s;
    }

    @Override
    public void init() {
        super.init();

        getGame().add(mSprite);

        if (isEnabled()) {
            getGame().add(mSpriteFire);
        }
    }

    @Override
    public void clean() {
        super.clean();

        getGame().remove(mSprite);

        if (isEnabled()) {
            getGame().remove(mSpriteFire);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (isInGame() && !enabled) {
            getGame().remove(mSpriteFire);
        }

        if (isInGame() && enabled) {
            getGame().add(mSpriteFire);
        }
    }

    @Override
    public void onDraw(Drawable sprite, Canvas canvas) {
        super.onDraw(sprite, canvas);

        canvas.rotate(mAngle);
    }

    @Override
    public void tick() {
        if (isEnabled()) {
            setDirection(getDirectionTo(getTarget()));
            mAngle = getAngleTo(getTarget());

            mSpriteFire.tick();
        }

        super.tick();
    }

    @Override
    protected void onTargetLost() {
        Enemy closest = (Enemy) getGame().get(Enemy.TYPE_ID)
                .min(distanceTo(getPosition()));

        if (closest == null) {
            getGame().remove(this);
        } else {
            setTarget(closest);
        }
    }

    @Override
    protected void onTargetReached() {
        getGame().add(new Explosion(getOrigin(), getTarget().getPosition(), mDamage, mRadius));
        getGame().remove(this);
    }
}
