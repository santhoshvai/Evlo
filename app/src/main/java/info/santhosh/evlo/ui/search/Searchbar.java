package info.santhosh.evlo.ui.search;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import java.lang.ref.WeakReference;

import info.santhosh.evlo.R;
import info.santhosh.evlo.ui.TransformingToolbar;

/**
 * A Toolbar with an EditText used for searching
 * <p>In a real life application you would hook up your TextWatcher to this method to track what the user is searching for</p>
 */
public class Searchbar extends TransformingToolbar {

    private EditText editText;
    WeakReference<onTextChanged> mOnTextChangedWeakReference;

    public Searchbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        inflate(getContext(), R.layout.merge_search, this);
        editText = (EditText) findViewById(R.id.toolbar_search_edittext);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final onTextChanged onTextChanged = mOnTextChangedWeakReference.get();
                if (onTextChanged != null) onTextChanged.onSearchChange(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void showContent() {
        super.showContent();
        editText.requestFocus();
    }

    public void clearText() {
        editText.setText(null);
    }

    public void setTextChangedListener(onTextChanged onTextChanged) {
        mOnTextChangedWeakReference = new WeakReference<>(onTextChanged);
    }

    interface onTextChanged {
        void onSearchChange(CharSequence s);
    }

}
