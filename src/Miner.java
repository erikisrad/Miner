import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;

@ScriptManifest(name = "Miner", description = "dolla dolla bill y'all", author = "erik",
        version = 1.0, category = Category.MINING, image = "")

public class Miner extends AbstractScript  {

    private ZenAntiBan z;
    private Utilities u;
    State state;
    Area safety = new Area(3161, 3493, 3168, 3486);
    Area bankArea = new Area(3180, 3447, 3185, 3433);
    Area boothArea = new Area(3186, 3444, 3186, 3435);
    Area mineArea = new Area(3179, 3379, 3184, 3371);
    Area primaryOre = new Area(3178, 3372, 3181, 3370);
    Area secondaryOre = new Area(3182, 3378, 3184, 3376);

    @Override
    public void onStart() {

        z = new ZenAntiBan(this);
        u= new Utilities();
        Logger.log("booting script...");
        if(!Walking.isRunEnabled()){
            Walking.toggleRun();
        }

    }

    @Override
    public int onLoop() {
        switch (getState()) {

            case CLOSING_DIALOGUE:
                Sleep.sleep(200, 400);
                Dialogues.spaceToContinue();
                break;

            case RETREATING:
                Logger.error("our health is low!");
                u.run_to_location(1, safety, -1);
                break;

            case IN_COMBAT:
                Logger.error("we are being attacked!");
                u.run_to_location(1, safety, -1);
                break;

            case WALKING_TO_BANK:
                u.inventory_open();
                u.run_to_location(33, bankArea, 60000);
                break;

            case OPENING_BANK:
                u.open_bank(boothArea);
                break;

            case BANKING:
                u.bank_deposit_all("all");
                break;

            case WALKING_TO_MINE:
                u.run_to_location(33, mineArea, 60000);
                break;

            case FINDING_ROCK:
                u.inventory_open();
                if(!u.others_static_in_area(primaryOre)){
                    Logger.debug("checked primary ore for players");
                    u.find_and_gather("Clay rocks", "Mine", primaryOre);

                }else if(!u.others_static_in_area(secondaryOre)) {
                    Logger.debug("checked secondary ore for players");
                    u.find_and_gather("Clay rocks", "Mine", secondaryOre);

                }else{
                    Logger.warn("area filled with other bots");
                }
                break;

            case MINING_ROCK:
                z.antiBan();
                break;

        }
        return 100;
    }

    private State getState(){

        if(Dialogues.canContinue()
                || Dialogues.inDialogue()){

            Logger.log("closing dialogue");
            state = State.CLOSING_DIALOGUE;

        }else if(Players.getLocal().getHealthPercent() < 66
                && !safety.contains(Players.getLocal().getTile())){

            Logger.log("retreating");
            state = State.RETREATING;

        }else if (Players.getLocal().isInCombat()
                && (Players.getLocal().getCharacterInteractingWithMe() != null)
                && !safety.contains(Players.getLocal().getTile())){

            Logger.log("in combat");
            state = State.IN_COMBAT;

        }else if(Inventory.isFull() && !bankArea.contains(Players.getLocal().getTile())){
            Logger.log("walking to bank");
            state = State.WALKING_TO_BANK;

        }else if (!Bank.isOpen()
                && !Inventory.isEmpty()
                && bankArea.contains(Players.getLocal().getTile())) {

            Logger.log("opening bank");
            state = State.OPENING_BANK;

        }else if (Bank.isOpen()
                && !Inventory.isEmpty()) {

            Logger.log("banking");
            state = State.BANKING;

        }else if(!Inventory.isFull()
                && !mineArea.contains(Players.getLocal().getTile())){

            Logger.log("walking to mine");
            state = State.WALKING_TO_MINE;

        }else if (!Inventory.isFull()
                && !Players.getLocal().isAnimating()
                && mineArea.contains(Players.getLocal().getTile())) {

            if(state != State.FINDING_ROCK) {
                Logger.log("finding rock");
            }
            state = State.FINDING_ROCK;

        }else if (!Inventory.isFull()
                && Players.getLocal().isAnimating()
                && mineArea.contains(Players.getLocal().getTile())) {

            if(state != State.MINING_ROCK) {
                Logger.log("mining rock");
            }
            state = State.MINING_ROCK;
        }

        return state;
    }






}
