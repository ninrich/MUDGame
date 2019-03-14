package mud;

class Player {
    private String name;
    private MUDGame currentGame;

    Player(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void connectToMUDGame (MUDGame game) {
        this.currentGame = game;
    }
}