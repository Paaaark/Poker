package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.sql.SQLOutput;
import java.util.*;
import java.lang.Thread;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Constants
    public static final long WAIT_TIME = 5000;
    public static final int TOAST_TIME = 2000;
    public static final int BIG_BLIND_AMOUNT = 20;
    public static final String HIDDEN = "-1";
    public static final int CHECKING_WINNER = 1001;

    Game game;
    // Buttons
    ImageView playerOneCardOne;
    ImageView playerOneCardTwo;
    ImageView playerTwoCardOne;
    ImageView playerTwoCardTwo;
    Button startButton;
    Button playerOneCheck;
    Button playerOneRaise;
    Button playerOneFold;
    Button playerTwoCheck;
    Button playerTwoRaise;
    Button playerTwoFold;
    // TextViews and ImageViews
    TextView playerOneBettedAmount;
    TextView playerTwoBettedAmount;
    TextView playerOneMoney;
    TextView playerTwoMoney;
    TextView playerOneToast;
    TextView playerTwoToast;
    TextView playerOneTurn;
    TextView playerTwoTurn;
    TextView playerOneResult;
    TextView playerTwoResult;
    TextView playerOneResultCards;
    TextView playerTwoResultCards;
    ConstraintLayout screen;
    ConstraintLayout startScreen;
    ArrayList<ImageView> sharedCardsImages;
    Handler handler;
    int pokerCardBack;
    boolean isCardChecked[] = new boolean[4];
    boolean isAllCardsChecked = false;
    int whoseTurn = 0;
    int alternateTurn = -1;
    ArrayList<Card> sharedCards;
    int[] cardImageResources;
    int extraHolding = 0;

    @Override
    public void onClick(View v) {
        if (game == null) {
            Toast.makeText(this, "Game hasn't started!", Toast.LENGTH_SHORT).show();
            return;
        }
        Card card;
        switch (v.getId()) {
//            case R.id.startButton:
//                setContentView(R.layout.activity_main); break;
            case R.id.wholeScreen:
                if (alternateTurn == CHECKING_WINNER) {
                    startGame();
                }
                break;
            case R.id.playerOneCardOne:
                showCard(playerOneCardOne, game.getPlayerOneCardOne(), 0);
                break;
            case R.id.playerOneCardTwo:
                showCard(playerOneCardTwo, game.getPlayerOneCardTwo(), 1);
                break;
            case R.id.playerTwoCardOne:
                showCard(playerTwoCardOne, game.getPlayerTwoCardOne(), 2);
                break;
            case R.id.playerTwoCardTwo:
                showCard(playerTwoCardTwo, game.getPlayerTwoCardTwo(), 3);
                break;
            case R.id.playerOneCheck:
                if (!isAllCardsChecked) {
                    showToast("Check your cards to proceed", playerOneToast);
                } else if (alternateTurn != 0) {
                    showToast("Wait for your turn!", playerOneToast);
                } else {
                    int diff = game.getPlayerTwoBetAmount() - game.getPlayerOneBetAmount();
                    game.playerOneBet(diff);
                    updateStanding();
                    changeTurn(1);
                    if (diff != 0) {
                        if (sharedCards.size() == 5) {
                            determineWinner();
                            //startGame();
                        } else {
                            showSharedCards();
                        }
                    }
                }
                break;
            case R.id.playerOneRaise:
                if (!isAllCardsChecked) {
                    showToast("Check your cards to proceed", playerOneToast);
                } else if (alternateTurn != 0) {
                    showToast("Wait for your turn!", playerOneToast);
                } else {
                    int diff = game.getPlayerTwoBetAmount() - game.getPlayerOneBetAmount();
                    diff = diff > 0 ? diff : 0;
                    game.playerOneBet(BIG_BLIND_AMOUNT + diff);
                    updateStanding();
                    changeTurn(1);
                }
                break;
            case R.id.playerOneFold:
                if (!isAllCardsChecked) {
                    showToast("Check your cards to proceed", playerOneToast);
                } else if (alternateTurn != 0) {
                    showToast("Wait for your turn!", playerOneToast);
                } else {
                    game.playerTwoWon();
                    updateStanding();
                    startGame();
                }
                break;
            case R.id.playerTwoCheck:
                if (!isAllCardsChecked) {
                    showToast("Check your cards to proceed", playerTwoToast);
                } else if (alternateTurn != 1) {
                    showToast("Wait for your turn!", playerTwoToast);
                } else {
                    int diff = game.getPlayerOneBetAmount() - game.getPlayerTwoBetAmount();
                    game.playerTwoBet(diff);
                    updateStanding();
                    changeTurn(0);
                    if (diff != 0) {
                        if (sharedCards.size() == 5) {
                            determineWinner();
                            //startGame();
                        } else {
                            showSharedCards();
                        }
                    }
                }
                break;
            case R.id.playerTwoRaise:
                if (!isAllCardsChecked) {
                    showToast("Check your cards to proceed", playerTwoToast);
                } else if (alternateTurn != 1) {
                    showToast("Wait for your turn!", playerTwoToast);
                } else {
                    int diff = game.getPlayerOneBetAmount() - game.getPlayerTwoBetAmount();
                    diff = diff > 0 ? diff : 0;
                    game.playerTwoBet(BIG_BLIND_AMOUNT + diff);
                    updateStanding();
                    changeTurn(0);
                }
                break;
            case R.id.playerTwoFold:
                if (!isAllCardsChecked) {
                    showToast("Check your cards to proceed", playerTwoToast);
                } else if (alternateTurn != 1) {
                    showToast("Wait for your turn!", playerTwoToast);
                } else {
                    game.playerOneWon();
                    updateStanding();
                    startGame();
                }
                break;
        }
    }

    public void bigBlindBet() {
        if (alternateTurn % 2 == 0) {
            showToast("Big Blind Bet", playerTwoToast);
            addBet(game.getPlayerTwo(), BIG_BLIND_AMOUNT, playerTwoBettedAmount, playerTwoMoney);
        } else {
            showToast("Big Blind Bet", playerOneToast);
            addBet(game.getPlayerOne(), BIG_BLIND_AMOUNT, playerOneBettedAmount, playerOneMoney);
        }
    }

    /**
     * Shows a player's card passed by the first argument
     * @param slot
     * @param card
     * @param index
     */
    public void showCard(ImageView slot, Card card, int index) {
        if (String.valueOf(slot.getTag()).equals(HIDDEN)) {
            if (card == null) {
                Toast.makeText(this, "Card not given yet!", Toast.LENGTH_SHORT).show();
            } else {
                slot.setImageResource(cardImageResources[card.getCardID()]);
                slot.setTag(Integer.toString(card.getCardID()));
                isCardChecked[index] = true;
            }
        } else {
            slot.setImageResource(pokerCardBack);
            slot.setTag(HIDDEN);
        }
        boolean flag = true;
        for (boolean isChecked: isCardChecked) {
            if (!isChecked) flag = false;
        }
        if (!isAllCardsChecked && flag) {
            alternateTurn = whoseTurn;
            bigBlindBet();
            changeTurn(alternateTurn);
        }
        isAllCardsChecked = flag;
    }

    /**
     * Displays whose turn it is. Odd number = player one, even number = player two
     * @param num
     */
    public void changeTurn(int num) {
        String msg;
        if (num % 2 == 0) msg = "Player one's turn";
        else msg = "Player two's turn";
        alternateTurn = num;
        playerOneTurn.setText(msg);
        playerTwoTurn.setText(msg);
        playerOneTurn.setVisibility(View.VISIBLE);
        playerTwoTurn.setVisibility(View.VISIBLE);
    }

    public void determineWinner() {
        alternateTurn = CHECKING_WINNER;
        int result[] = game.getWinner(sharedCards);
        playerOneTurn.setVisibility(View.GONE);
        playerTwoTurn.setVisibility(View.GONE);
        playerOneResult.setText(Card.combinationToString(result[1]));
        playerOneResult.setVisibility(View.VISIBLE);
        String playerOneDisplay = Card.toString(result[2]) + ", " + Card.toString(result[3]) +
                                  ", " + Card.toString(result[4]) + ", " + Card.toString(result[5])
                                  + ", " + Card.toString(result[6]);
        playerOneResultCards.setText(playerOneDisplay);
        playerOneResultCards.setVisibility(View.VISIBLE);
        playerTwoResult.setText(Card.combinationToString(result[7]));
        playerTwoResult.setVisibility(View.VISIBLE);
        String playerTwoDisplay = Card.toString(result[8]) + ", " + Card.toString(result[9]) +
                                  ", " + Card.toString(result[10]) + ", " + Card.toString(result[11])
                                  + ", " + Card.toString(result[12]);
        playerTwoResultCards.setText(playerTwoDisplay);
        playerTwoResultCards.setVisibility(View.VISIBLE);
        showToast("Tap anywhere to continue", playerOneToast);
        showToast("Tap anywhere to continue", playerTwoToast);
    }

    /**
     * Open shared cards based on the number of cards open already
     */
    public void showSharedCards() {
        int start; int end;
        if (sharedCards.size() == 0) {
            start = 0; end = 3;
        } else if (sharedCards.size() == 3) {
            start = 3; end = 4;
        } else if (sharedCards.size() == 4) {
            start = 4; end = 5;
        } else {
            start = 0; end = 0;
        }
        for (int i = start; i < end; i++) {
            Card card = game.pickCard();
            card.setPlaceID(Card.SHARED_CARDS_ID + i);
            sharedCards.add(card);
            sharedCardsImages.get(i).setImageResource(cardImageResources[card.getCardID()]);
            sharedCardsImages.get(i).setTag(Integer.toString(card.getCardID()));
        }
    }

    /**
     * Adds amount to the player's bet. Also updates betAmount and holding of the respective player
     * @param player player to add the betting to
     * @param amount amount of the added bet
     * @param betAmountText textView of the respective player
     * @param moneyText textView of the respective player
     */
    public void addBet(Player player, int amount, TextView betAmountText, TextView moneyText) {
        player.addBetAmount(amount);
        betAmountText.setText(Integer.toString(player.getBetAmount()));
        moneyText.setText(Integer.toString(player.getHolding()));
    }

    /**
     * Update betAmount and holding of both players
     */
    public void updateStanding() {
        playerOneBettedAmount.setText(Integer.toString(game.getPlayerOneBetAmount()));
        playerOneMoney.setText(Integer.toString(game.getPlayerOneHolding()));
        playerTwoBettedAmount.setText(Integer.toString(game.getPlayerTwoBetAmount()));
        playerTwoMoney.setText(Integer.toString(game.getPlayerTwoHolding()));
    }

    /**
     * Start a new game. Shuffle the deck. Give out cards. Empty shared cards.
     * Hide "Turn display". Display "Check your cards"
     */
    public void startGame() {
        sharedCards.clear();
        for (ImageView card: sharedCardsImages) {
            card.setImageResource(R.mipmap.poker_card_back);
            card.setTag(HIDDEN);
        }
        playerOneCardOne.setImageResource(pokerCardBack);
        playerOneCardOne.setTag(HIDDEN);
        playerOneCardTwo.setImageResource(pokerCardBack);
        playerOneCardTwo.setTag(HIDDEN);
        playerTwoCardOne.setImageResource(pokerCardBack);
        playerTwoCardOne.setTag(HIDDEN);
        playerTwoCardTwo.setImageResource(pokerCardBack);
        playerTwoCardTwo.setTag(HIDDEN);
        playerOneTurn.setVisibility(View.GONE);
        playerTwoTurn.setVisibility(View.GONE);
        playerOneResult.setVisibility(View.GONE);
        playerTwoResult.setVisibility(View.GONE);
        playerOneResultCards.setVisibility(View.GONE);
        playerTwoResultCards.setVisibility(View.GONE);
        updateStanding();
        if (game.numCardLeft() < 9) {
            Toast.makeText(this, "Not Enough Cards Left, New Deck will be shuffled",
                    Toast.LENGTH_SHORT).show();
            game.getNewDeck();
        }
        game.giveOutCards();
        Arrays.fill(isCardChecked, false);
        isAllCardsChecked = false;
        showToast("Check your cards", playerOneToast);
        showToast("Check your cards", playerTwoToast);
        whoseTurn = whoseTurn == 1 ? 0 : 1;
        alternateTurn = -1;
    }

    /**
     * Displays message on an appropriate textview for TOAST_TIME duration
     * @param msg Message to be displayed
     * @param toast TextView to display on
     */
    public void showToast(String msg, TextView toast) {
        System.out.println("showToast() triggered");
        toast.setText(msg);
        toast.setVisibility(View.VISIBLE);
        Runnable r = new Runnable() {
            public void run() {
                toast.setVisibility(View.GONE);
            }
        };
        handler.postDelayed(r, TOAST_TIME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.app_start_screen);

        setContentView(R.layout.activity_main);

        // Adding image resources to the arraylist

        cardImageResources = new int[52];
        cardImageResources[0] = R.mipmap.two_of_diamonds;
        cardImageResources[1] = R.mipmap.two_of_clubs;
        cardImageResources[2] = R.mipmap.two_of_hearts;
        cardImageResources[3] = R.mipmap.two_of_spades;
        cardImageResources[4] = R.mipmap.three_of_diamonds;
        cardImageResources[5] = R.mipmap.three_of_clubs;
        cardImageResources[6] = R.mipmap.three_of_hearts;
        cardImageResources[7] = R.mipmap.three_of_spades;
        cardImageResources[8] = R.mipmap.four_of_diamonds;
        cardImageResources[9] = R.mipmap.four_of_clubs;
        cardImageResources[10] = R.mipmap.four_of_hearts;
        cardImageResources[11] = R.mipmap.four_of_spades;
        cardImageResources[12] = R.mipmap.five_of_diamonds;
        cardImageResources[13] = R.mipmap.five_of_clubs;
        cardImageResources[14] = R.mipmap.five_of_hearts;
        cardImageResources[15] = R.mipmap.five_of_spades;
        cardImageResources[16] = R.mipmap.six_of_diamonds;
        cardImageResources[17] = R.mipmap.six_of_clubs;
        cardImageResources[18] = R.mipmap.six_of_hearts;
        cardImageResources[19] = R.mipmap.six_of_spades;
        cardImageResources[20] = R.mipmap.seven_of_diamonds;
        cardImageResources[21] = R.mipmap.seven_of_clubs;
        cardImageResources[22] = R.mipmap.seven_of_hearts;
        cardImageResources[23] = R.mipmap.seven_of_spades;
        cardImageResources[24] = R.mipmap.eight_of_diamonds;
        cardImageResources[25] = R.mipmap.eight_of_clubs;
        cardImageResources[26] = R.mipmap.eight_of_hearts;
        cardImageResources[27] = R.mipmap.eight_of_spades;
        cardImageResources[28] = R.mipmap.nine_of_diamonds;
        cardImageResources[29] = R.mipmap.nine_of_clubs;
        cardImageResources[30] = R.mipmap.nine_of_hearts;
        cardImageResources[31] = R.mipmap.nine_of_spades;
        cardImageResources[32] = R.mipmap.ten_of_diamonds;
        cardImageResources[33] = R.mipmap.ten_of_clubs;
        cardImageResources[34] = R.mipmap.ten_of_hearts;
        cardImageResources[35] = R.mipmap.ten_of_spades;
        cardImageResources[36] = R.mipmap.jack_of_diamonds;
        cardImageResources[37] = R.mipmap.jack_of_clubs;
        cardImageResources[38] = R.mipmap.jack_of_clubs;
        cardImageResources[39] = R.mipmap.jack_of_spades;
        cardImageResources[40] = R.mipmap.queen_of_diamonds;
        cardImageResources[41] = R.mipmap.queen_of_clubs;
        cardImageResources[42] = R.mipmap.queen_of_hearts;
        cardImageResources[43] = R.mipmap.queen_of_spades;
        cardImageResources[44] = R.mipmap.king_of_clubs;
        cardImageResources[45] = R.mipmap.king_of_clubs;
        cardImageResources[46] = R.mipmap.king_of_hearts;
        cardImageResources[47] = R.mipmap.king_of_spades;
        cardImageResources[48] = R.mipmap.ace_of_diamonds;
        cardImageResources[49] = R.mipmap.ace_of_clubs;
        cardImageResources[50] = R.mipmap.ace_of_hearts;
        cardImageResources[51] = R.mipmap.ace_of_spades;

        game = new Game();
        handler = new Handler();
        sharedCards = new ArrayList<Card>();
        sharedCardsImages = new ArrayList<ImageView>();
        pokerCardBack = R.mipmap.poker_card_back;

        // Add click listeners to buttons
        playerOneCardOne = findViewById(R.id.playerOneCardOne);
        playerOneCardOne.setOnClickListener(this);
        playerOneCardTwo = findViewById(R.id.playerOneCardTwo);
        playerOneCardTwo.setOnClickListener(this);
        playerTwoCardOne = findViewById(R.id.playerTwoCardOne);
        playerTwoCardOne.setOnClickListener(this);
        playerTwoCardTwo = findViewById(R.id.playerTwoCardTwo);
        playerTwoCardTwo.setOnClickListener(this);
        playerOneCheck = findViewById(R.id.playerOneCheck);
        playerOneCheck.setOnClickListener(this);
        playerOneRaise = findViewById(R.id.playerOneRaise);
        playerOneRaise.setOnClickListener(this);
        playerOneFold = findViewById(R.id.playerOneFold);
        playerOneFold.setOnClickListener(this);
        playerTwoCheck = findViewById(R.id.playerTwoCheck);
        playerTwoCheck.setOnClickListener(this);
        playerTwoRaise = findViewById(R.id.playerTwoRaise);
        playerTwoRaise.setOnClickListener(this);
        playerTwoFold = findViewById(R.id.playerTwoFold);
        playerTwoFold.setOnClickListener(this);
        screen = findViewById(R.id.wholeScreen);
        screen.setOnClickListener(this);
        //startScreen = findViewById(R.id.startScreen);
        //startButton = findViewById(R.id.startButton);
        //startButton.setOnClickListener(this);
        // Initialize text views
        playerOneBettedAmount = findViewById(R.id.playerOneBettedAmount);
        playerOneBettedAmount.setText("0");
        playerTwoBettedAmount = findViewById(R.id.playerTwoBettedAmount);
        playerTwoBettedAmount.setText("0");
        playerOneMoney = findViewById(R.id.playerOneMoney);
        playerOneMoney.setText(Integer.toString(game.getPlayerOneHolding()));
        playerTwoMoney = findViewById(R.id.playerTwoMoney);
        playerTwoMoney.setText(Integer.toString(game.getPlayerTwoHolding()));
        playerOneToast = findViewById(R.id.playerOneToast);
        playerTwoToast = findViewById(R.id.playerTwoToast);
        sharedCardsImages.add(findViewById(R.id.sharedOne));
        sharedCardsImages.get(0).setTag(HIDDEN);
        sharedCardsImages.add(findViewById(R.id.sharedTwo));
        sharedCardsImages.get(1).setTag(HIDDEN);
        sharedCardsImages.add(findViewById(R.id.sharedThree));
        sharedCardsImages.get(2).setTag(HIDDEN);
        sharedCardsImages.add(findViewById(R.id.sharedFour));
        sharedCardsImages.get(3).setTag(HIDDEN);
        sharedCardsImages.add(findViewById(R.id.sharedFive));
        sharedCardsImages.get(4).setTag(HIDDEN);
        playerOneTurn = findViewById(R.id.playerOneTurn);
        playerTwoTurn = findViewById(R.id.playerTwoTurn);
        playerOneResult = findViewById(R.id.playerOneResult);
        playerTwoResult = findViewById(R.id.playerTwoResult);
        playerOneResultCards = findViewById(R.id.playerOneResultCards);
        playerTwoResultCards = findViewById(R.id.playerTwoResultCards);

        startGame();
    }
}