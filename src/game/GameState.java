package game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

public class GameState extends BaseAppState {

    private Application app;
    private int level;

    public GameState(int level) {
        this.level = level;
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;
    }

    @Override
    public void onEnable() {
        getStateManager().attach(new CubeState());
    }

    @Override
    public void onDisable() {
        getStateManager().detach(getStateManager().getState(CubeState.class));
    }

    @Override
    protected void cleanup(Application app) {}
}
