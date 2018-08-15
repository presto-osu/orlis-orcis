package koeln.mop.elpeefpe;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import koeln.mop.elpeefpe.R;

/**
 * Created by Andreas Streichardt on 19.06.2016.
 */
public class ElpeEfpeControlView extends LinearLayout {
    private Button plus;
    private Button minus;

    public ElpeEfpeControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ElpeEfpeControlView, 0, 0);
        DamageType damageType = DamageType.values()[a.getInt(R.styleable.ElpeEfpeControlView_damageType, 0)];
        init(damageType);

        a.recycle();
    }

    public ElpeEfpeControlView(Context context) {
        this(context, null);
    }

    private void init(DamageType damageType) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.elpeefpe_control, this);

        ImageView image = (ImageView) findViewById(R.id.damage);

        if (damageType == DamageType.KANALISIERT) {
            image.setImageResource(R.drawable.dmg_kanalisiert);
        } else if (damageType == DamageType.ERSCHOEPFT) {
            image.setImageResource(R.drawable.dmg_erschoepft);
        } else if (damageType == DamageType.VERZEHRT) {
            image.setImageResource(R.drawable.dmg_verzehrt);
        }
    }

    public void onAdd(OnClickListener listener) {
        ImageButton plusButton = (ImageButton) findViewById(R.id.plus);
        plusButton.setOnClickListener(listener);
    }

    public void onRemove(OnClickListener listener) {
        ImageButton minusButton = (ImageButton) findViewById(R.id.minus);
        minusButton.setOnClickListener(listener);
    }
}
