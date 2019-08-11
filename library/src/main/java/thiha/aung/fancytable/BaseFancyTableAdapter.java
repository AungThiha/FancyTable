package thiha.aung.fancytable;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

public abstract class BaseFancyTableAdapter implements FancyTableAdapter {

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    /**
     * Notifies the attached observers that the underlying data is no longer
     * valid or available. Once invoked this adapter is no longer valid and
     * should not report further data set changes.
     */
    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public int getDockedRowCount() {
        return 1;
    }

    @Override
    public int getDockedColumnCount() {
        return 1;
    }

    @Override
    public boolean isRowOneColumn(int row) {
        return false;
    }

    @Override
    public boolean isRowShadowShown() {
        return true;
    }

    @Override
    public boolean isColumnShadowShown() {
        return true;
    }
}
