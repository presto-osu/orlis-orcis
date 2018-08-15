package im.r_c.android.jigsaw.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import im.r_c.android.jigsaw.App;
import im.r_c.android.jigsaw.fragment.GameFragment;
import im.r_c.android.jigsaw.R;
import im.r_c.android.jigsaw.fragment.WinFragment;
import im.r_c.android.jigsaw.util.IOUtils;
import im.r_c.android.jigsaw.util.L;
import im.r_c.android.jigsaw.util.UIUtils;
import me.drakeet.mailotto.Mail;
import me.drakeet.mailotto.Mailbox;
import me.drakeet.mailotto.OnMailReceived;

/**
 * Jigsaw
 * Created by richard on 16/5/15.
 */
public class GameActivity extends AppCompatActivity {
    private static final String TAG = "GameActivity";
    public static final int SPAN_COUNT = 3;
    public static final int BLANK_BRICK = 8;
    public static final int[][] GOAL_STATUS = {{0, 1, 2}, {3, 4, 5}, {6, 7, BLANK_BRICK}};
    public static final int MAIL_GAME_STARTED = 100;
    public static final int MAIL_STEP_MOVED = 101;
    public static final int MAIL_GAME_WON = 102;
    public static final int REQUEST_CODE_CHOOSE_PICTURE = 200;

    private Bitmap mFullBitmap;
    private Bitmap[] mBitmapBricks = new Bitmap[SPAN_COUNT * SPAN_COUNT];
    private Timer mTimer;
    private long mStartTime;
    private int mStepCount;

    private TextView mTvTime;
    private TextView mTvStep;
    private Button mBtnChooseAndStart;
    private GameFragment mGameFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            window.setEnterTransition(new Explode());
            window.setExitTransition(new Explode());
        }
        setContentView(R.layout.activity_game);

        Mailbox.getInstance().atHome(this);

        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvStep = (TextView) findViewById(R.id.tv_step);
        mBtnChooseAndStart = (Button) findViewById(R.id.btn_choose_and_start);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mailbox.getInstance().leave(this);
    }

    private void startActivityForNewPicture() {
        Intent intent = new Intent(this, ChooseActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(
                    intent, REQUEST_CODE_CHOOSE_PICTURE,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            findViewById(R.id.btn_change_picture),
                            getString(R.string.bottom_button_trans_name)).toBundle());
        } else {
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE_PICTURE:
                if (resultCode == RESULT_OK) {
                    handleChooseResult(data.getData());
                }
                break;
        }
    }

    private void handleChooseResult(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

            // Delete the cache file CropImage generated
            IOUtils.deleteFile(uri.getPath());

            // Scale the bitmap to a proper size, avoiding waste of memory
            View container = findViewById(R.id.fl_board_container);
            assert container != null;
            int paddingHorizontal = container.getPaddingLeft() + container.getPaddingRight();
            int paddingVertical = container.getPaddingTop() + container.getPaddingBottom();
            mFullBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    container.getWidth() - paddingHorizontal,
                    container.getHeight() - paddingVertical,
                    false);

            cutBitmapIntoPieces();
            mBitmapBricks[SPAN_COUNT * SPAN_COUNT - 1] = BitmapFactory.decodeResource(getResources(), R.drawable.blank_brick);

            startNewGame();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void cutBitmapIntoPieces() {
        int dividerWidth = (int) getResources().getDimension(R.dimen.brick_divider_width);
        int brickWidth = (mFullBitmap.getWidth() - dividerWidth * (SPAN_COUNT - 1)) / SPAN_COUNT;
        int brickHeight = (mFullBitmap.getHeight() - dividerWidth * (SPAN_COUNT - 1)) / SPAN_COUNT;
        for (int i = 0; i < SPAN_COUNT; i++) {
            for (int j = 0; j < SPAN_COUNT; j++) {
                mBitmapBricks[i * SPAN_COUNT + j] = Bitmap.createBitmap(
                        mFullBitmap,
                        j * (brickWidth + dividerWidth),
                        i * (brickHeight + dividerWidth),
                        brickWidth, brickHeight);
            }
        }
    }

    private void startNewGame() {
        mBtnChooseAndStart.setVisibility(View.GONE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_board_container, GameFragment.newInstance(mBitmapBricks, GOAL_STATUS))
                .commit();
    }

    @OnMailReceived
    public void onMailReceived(Mail mail) {
        if (mail.from == GameFragment.class) {
            switch ((int) mail.content) {
                case MAIL_GAME_STARTED:
                    L.d(TAG, "Game started");
                    onGameStarted();
                    break;
                case MAIL_STEP_MOVED:
                    L.d(TAG, "Moved");
                    onStepMoved();
                    break;
                case MAIL_GAME_WON:
                    L.d(TAG, "Game won");
                    onGameWon();
                    break;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void onGameStarted() {
        mStepCount = 0;
        mTvStep.setText(String.valueOf(mStepCount));
        mTvTime.setText("00:00");

        mStartTime = System.currentTimeMillis();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long nowTime = System.currentTimeMillis();
                        Date span = new Date(nowTime - mStartTime);
                        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                        mTvTime.setText(format.format(span));
                    }
                });
            }
        }, 0, 1000);
    }

    private void onStepMoved() {
        mStepCount++;
        mTvStep.setText(String.valueOf(mStepCount));
    }

    private void onGameWon() {
        mTimer.cancel();
        mTimer.purge();
        App.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fl_board_container, WinFragment.newInstance(mFullBitmap))
                        .commit();
                UIUtils.toast(
                        GameActivity.this,
                        String.format(getString(R.string.win_prompt_format), mTvTime.getText().toString(), mTvStep.getText().toString()),
                        true);
            }
        }, 500);
    }

    public void changePicture(View view) {
        startActivityForNewPicture();
    }

    public void restart(View view) {
        if (mFullBitmap == null) {
            // Not started, so cannot restart
            UIUtils.toast(this, getString(R.string.not_started));
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.restart))
                .setMessage(getString(R.string.confirm_restart_msg))
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startNewGame();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void lookUpOriginalPicture(View view) {
        if (mFullBitmap == null) {
            // Not started, so cannot restart
            UIUtils.toast(this, getString(R.string.not_started));
            return;
        }

        View alertView = View.inflate(this, R.layout.dialog_loop_up, null);
        ImageView imageView = (ImageView) alertView.findViewById(R.id.iv_image);
        imageView.setImageBitmap(mFullBitmap);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(alertView)
                .create();
        alertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dialog.dismiss();
                return true;
            }
        });
        dialog.show();
    }

    public void chooseAndStart(View view) {
        startActivityForNewPicture();
    }
}
