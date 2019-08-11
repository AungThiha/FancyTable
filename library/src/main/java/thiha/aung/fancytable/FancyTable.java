package thiha.aung.fancytable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

import static thiha.aung.fancytable.FancyTableAdapter.FILL_WIDTH_VIEW;

/**
 * This view shows a table which can scroll in both directions.
 * Can define how many rows or columns needs to be fixed/docked: rows fixed on the top and columns fixed on the left
 * Can add headers. Headers are not horizontally scrollable
 */
public class FancyTable extends ViewGroup {

    private final ImageView[] shadows;
    private final int shadowSize;
    private final int minimumVelocity;
    private final int maximumVelocity;
    private final Flinger flinger;

    private int currentX;
    private int currentY;
    private FancyTableAdapter adapter;
    private int scrollX;
    private int scrollY;
    private int firstScrollableRow;
    private int firstScrollableColumn;
    private int dockedRowCount;
    private int dockedColumnCount;
    private int[] widths;
    private int[] heights;
    private List<List<View>> bodyViewTable;
    private int rowCount;
    private int columnCount;
    private int width;
    private int height;
    private Recycler recycler;
    private TableAdapterDataSetObserver tableAdapterDataSetObserver;
    private boolean needRelayout;
    private VelocityTracker velocityTracker;
    private int touchSlop;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public FancyTable(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     *
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public FancyTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.bodyViewTable = new ArrayList<>();

        this.needRelayout = true;

        this.shadows = new ImageView[4];
        this.shadows[0] = new ImageView(context);
        this.shadows[0].setImageResource(R.drawable.shadow_left);
        this.shadows[1] = new ImageView(context);
        this.shadows[1].setImageResource(R.drawable.shadow_top);
        this.shadows[2] = new ImageView(context);
        this.shadows[2].setImageResource(R.drawable.shadow_right);
        this.shadows[3] = new ImageView(context);
        this.shadows[3].setImageResource(R.drawable.shadow_bottom);

        this.shadowSize = getResources().getDimensionPixelSize(R.dimen.shadow_size);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        this.flinger = new Flinger(context);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        this.touchSlop = configuration.getScaledTouchSlop();
        this.minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.maximumVelocity = configuration.getScaledMaximumFlingVelocity();

        this.setWillNotDraw(false);
    }

    /**
     * Returns the adapter currently associated with this widget.
     *
     * @return The adapter used to provide this view's content.
     */
    public FancyTableAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the data behind this TableFixHeaders.
     *
     * @param adapter The TableAdapter which is responsible for maintaining the data
     *                backing this list and for producing a view to represent an
     *                item in that data set.
     */
    public void setAdapter(FancyTableAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(tableAdapterDataSetObserver);
        }

        this.adapter = adapter;
        tableAdapterDataSetObserver = new TableAdapterDataSetObserver();
        this.adapter.registerDataSetObserver(tableAdapterDataSetObserver);

        this.recycler = new Recycler(adapter.getViewTypeCount());

        scrollX = 0;
        scrollY = 0;

        firstScrollableRow = adapter.getDockedRowCount();
        firstScrollableColumn = adapter.getDockedColumnCount();

        needRelayout = true;
        requestLayout();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                currentX = (int) event.getRawX();
                currentY = (int) event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int x2 = Math.abs(currentX - (int) event.getRawX());
                int y2 = Math.abs(currentY - (int) event.getRawY());
                if (x2 > touchSlop || y2 > touchSlop) {
                    intercept = true;
                }
                break;
            }
        }
        return intercept;
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);

        final int typeView = (Integer) view.getTag(R.id.tag_type_view);
        if (typeView != FancyTableAdapter.IGNORE_ITEM_VIEW_TYPE) {
            recycler.addRecycledView(view, typeView);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (needRelayout || changed) {
            needRelayout = false;
            resetTable();

            if (adapter != null) {
                width = r - l;
                height = b - t;

                int left, top, right, bottom;

                right = Math.min(width, sumArray(widths));
                bottom = Math.min(height, sumArray(heights));

                if (adapter.isColumnShadowShown()){
                    int columnShadowLeft = sumArray(widths, 0, dockedColumnCount);
                    addShadow(shadows[0], columnShadowLeft, 0, columnShadowLeft + shadowSize, bottom);
                }

                if (adapter.isRowShadowShown()){
                    int rowShadowTop = sumArray(heights, 0, dockedRowCount);
                    addShadow(shadows[1], 0, rowShadowTop, right, rowShadowTop + shadowSize);
                }
                //                addShadow(shadows[2], right - shadowSize, 0, right, bottom);
                //                addShadow(shadows[3], 0, bottom - shadowSize, right, bottom);

                scrollBounds();
                adjustFirstCellsAndScroll();


                // add table view
                top = 0;
                for (int i = 0; i < rowCount && top < height; i++) {
                    bottom = top + heights[i];
                    left = 0;
                    List<View> list = new ArrayList<>();

                    if (adapter.isRowOneColumn(i)){
                        final View view = makeAndSetup(i, 0, 0, top, width, bottom);
                        view.setTag(R.id.tag_column_type, FILL_WIDTH_VIEW);
                        list.add(view);
                    }else{
                        for (int j = 0; j < columnCount && left < width; j++) {
                            right = left + widths[j];
                            final View view = makeAndSetup(i, j, left, top, right, bottom);
                            view.setTag(R.id.tag_column_type, null);
                            list.add(view);
                            left = right;
                        }
                    }


                    bodyViewTable.add(list);
                    top = bottom;
                }

                shadowsVisibility();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) { // If we do not have velocity tracker
            velocityTracker = VelocityTracker.obtain(); // then get one
        }
        velocityTracker.addMovement(event); // add this movement to it

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!flinger.isFinished()) { // If scrolling, then stop now
                    flinger.forceFinished();
                }
                currentX = (int) event.getRawX();
                currentY = (int) event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int x2 = (int) event.getRawX();
                final int y2 = (int) event.getRawY();
                final int diffX = currentX - x2;
                final int diffY = currentY - y2;

                Log.d(FancyTable.class.getSimpleName(),
                        String.format(
                                "scrollX currentX=%d, currentY=%d, x2=%d, y2=%d, diffX=%d, diffY=%d",
                                currentX,
                                currentY,
                                x2,
                                y2,
                                diffX,
                                diffY
                        ));

                currentX = x2;
                currentY = y2;

                scrollBy(diffX, diffY);
                break;
            }
            case MotionEvent.ACTION_UP: {
                final VelocityTracker velocityTracker = this.velocityTracker;
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                int velocityY = (int) velocityTracker.getYVelocity();

                if (Math.abs(velocityX) > minimumVelocity || Math.abs(velocityY) > minimumVelocity) {
                    flinger.start(getActualScrollX(), getActualScrollY(), velocityX, velocityY, getMaxScrollX(), getMaxScrollY());
                } else {
                    if (this.velocityTracker != null) { // If the velocity less than threshold
                        this.velocityTracker.recycle(); // recycle the tracker
                        this.velocityTracker = null;
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (needRelayout) {
            scrollX = x;
            firstScrollableColumn = adapter == null ? 0 : adapter.getDockedColumnCount();

            scrollY = y;
            firstScrollableRow = adapter == null ? 0 : adapter.getDockedRowCount();
        } else {
            scrollBy(x - sumArray(widths, dockedColumnCount, firstScrollableColumn) - scrollX, y - sumArray(heights, dockedRowCount, firstScrollableRow) - scrollY);
        }
    }

    @Override
    public void scrollBy(int x, int y) {

        Log.d(FancyTable.class.getSimpleName(),
                String.format(
                        "original scrollX=%d, scrollY=%d",
                        scrollX,
                        scrollY
                ));

        scrollX += x;
        scrollY += y;

        Log.d(FancyTable.class.getSimpleName(),
                String.format(
                        "before scrollX=%d, scrollY=%d",
                        scrollX,
                        scrollY
                ));

        if (needRelayout) {
            return;
        }

        scrollBounds();

        Log.d(FancyTable.class.getSimpleName(),
                String.format(
                        "before scrollX=%d, scrollY=%d",
                        scrollX,
                        scrollY
                ));

        /*
         * TODO Improve the algorithm. Think big diagonal movements. If we are
         * in the top left corner and scrollBy to the opposite corner. We will
         * have created the views from the top right corner on the X part and we
         * will have eliminated to generate the right at the Y.
         */
        List<View> rowViewList = getRowViewList();
        if (scrollX == 0 || rowViewList.size() < dockedColumnCount) {
            // no op
        } else if (scrollX > 0) {
            while (widths[firstScrollableColumn] < scrollX) {
                if (!rowViewList.isEmpty()) {
                    removeLeft();
                }
                scrollX -= widths[firstScrollableColumn];
                firstScrollableColumn++;
            }
            while (getFilledWidth() < width) {
                addRight();
            }
        } else {

            int currentLastColumn = firstScrollableColumn - dockedColumnCount + rowViewList.size() - 1;
            Log.d(FancyTable.class.getSimpleName(),
                    String.format(
                            "firstScrollableColumn=%d, currentLastColumn=%d, rowViewList.size()=%d",
                            firstScrollableColumn,
                            currentLastColumn,
                            rowViewList.size()
                    )
            );
            while (!rowViewList.isEmpty() && getFilledWidth() - widths[currentLastColumn] >= width) {
                removeRight();
            }
            if (rowViewList.isEmpty()) {
                while (scrollX < 0) {
                    scrollX += widths[firstScrollableColumn];
                    firstScrollableColumn--;
                }
                while (getFilledWidth() < width) {
                    addRight();
                }
            } else {
                while (0 > scrollX) {
                    addLeft();
                    firstScrollableColumn--;
                    scrollX += widths[firstScrollableColumn];
                }
            }
        }

        if (scrollY == 0 || bodyViewTable.size() < dockedRowCount) {
            // no op
        } else if (scrollY > 0) {
            while (heights[firstScrollableRow] < scrollY) {
                if (!bodyViewTable.isEmpty()) {
                    removeTop();
                }
                scrollY -= heights[firstScrollableRow];
                firstScrollableRow++;
            }
            while (getFilledHeight() < height) {
                addBottom();
            }
        } else {
            int currentLastRow = firstScrollableRow - dockedRowCount + bodyViewTable.size() - 1;
            while (!bodyViewTable.isEmpty()
                    && getFilledHeight() - heights[currentLastRow] >= height) {
                removeBottom();
            }
            if (bodyViewTable.isEmpty()) {
                while (scrollY < 0) {
                    scrollY += heights[firstScrollableRow];
                    firstScrollableRow--;
                }
                while (getFilledHeight() < height) {
                    addBottom();
                }
            } else {
                while (0 > scrollY) {
                    addTop();
                    firstScrollableRow--;
                    scrollY += heights[firstScrollableRow];
                }
            }
        }

        repositionViews();

        shadowsVisibility();

        awakenScrollBars();
    }

    private List<View> getRowViewList(){
        for (List<View > list : bodyViewTable){
            if (!(list.size() > 0 && FILL_WIDTH_VIEW.equals(list.get(0).getTag(R.id.tag_column_type)))){
                return list;
            }
        }
        return bodyViewTable.get(0);
    }

    /*
     * The base measure
     */
    @Override
    protected int computeHorizontalScrollRange() {
        return width;
    }

    /*
     * The expected value is between 0 and computeHorizontalScrollRange() - computeHorizontalScrollExtent()
     */
    @Override
    protected int computeHorizontalScrollOffset() {
        final float maxScrollX = sumArray(widths) - width;
        final float percentageOfViewScrolled = getActualScrollX() / maxScrollX;
        int scrollStartLeft = sumArray(widths, 0, dockedColumnCount);
        final int maxHorizontalScrollOffset = width - scrollStartLeft - computeHorizontalScrollExtent();

        return scrollStartLeft + Math.round(percentageOfViewScrolled * maxHorizontalScrollOffset);
    }

    /*
     * The expected value is: percentageOfViewScrolled * computeHorizontalScrollRange()
     */
    @Override
    protected int computeHorizontalScrollExtent() {
        int scrollStartLeft = sumArray(widths, 0, dockedColumnCount);
        final float tableSize = width - scrollStartLeft;
        final float contentSize = sumArray(widths, dockedColumnCount, widths.length);
        final float percentageOfVisibleView = tableSize / contentSize;

        return Math.round(percentageOfVisibleView * tableSize);
    }

    /*
     * The base measure
     */
    @Override
    protected int computeVerticalScrollRange() {
        return height;
    }

    /*
     * The expected value is between 0 and computeVerticalScrollRange() - computeVerticalScrollExtent()
     */
    @Override
    protected int computeVerticalScrollOffset() {
        final float maxScrollY = sumArray(heights) - height;
        final float percentageOfViewScrolled = getActualScrollY() / maxScrollY;
        int scrollStartTop = sumArray(heights, 0, dockedRowCount);
        final int maxHorizontalScrollOffset = height - scrollStartTop - computeVerticalScrollExtent();

        return scrollStartTop + Math.round(percentageOfViewScrolled * maxHorizontalScrollOffset);
    }

    /*
     * The expected value is: percentageOfViewScrolled * computeVerticalScrollRange()
     */
    @Override
    protected int computeVerticalScrollExtent() {
        int scrollStartTop = sumArray(heights, 0, dockedRowCount);
        final float tableSize = height - scrollStartTop;
        final float contentSize = sumArray(heights, dockedRowCount, heights.length);
        final float percentageOfVisibleView = tableSize / contentSize;

        return Math.round(percentageOfVisibleView * tableSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int w;
        final int h;

        if (adapter != null) {

            this.rowCount = adapter.getRowCount();
            this.columnCount = adapter.getColumnCount();
            this.dockedRowCount = adapter.getDockedRowCount();
            this.dockedColumnCount = adapter.getDockedColumnCount();

            widths = new int[columnCount];
            heights = new int[rowCount];

            for (int i = 0; i < columnCount; i++) {
                widths[i] += adapter.getWidth(i);
            }

            for (int i = 0; i < rowCount; i++) {
                heights[i] += adapter.getHeight(i);
            }

            if (widthMode == MeasureSpec.AT_MOST) {
                w = Math.min(widthSize, sumArray(widths));
            } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                w = sumArray(widths);
            } else {
                w = widthSize;
                int sumArray = sumArray(widths);
                if (sumArray < widthSize) {
                    final float factor = widthSize / (float) sumArray;
                    for (int i = 1; i < widths.length; i++) {
                        widths[i] = Math.round(widths[i] * factor);
                    }
                    widths[0] = widthSize - sumArray(widths, 1, widths.length);
                }
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                h = Math.min(heightSize, sumArray(heights));
            } else if (heightMode == MeasureSpec.UNSPECIFIED) {
                h = sumArray(heights);
            } else {
                h = heightSize;
            }
        } else {
            if (heightMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
                w = 0;
                h = 0;
            } else {
                w = widthSize;
                h = heightSize;
            }
        }

        if (firstScrollableRow >= rowCount || getMaxScrollY() - getActualScrollY() < 0) {
            firstScrollableRow = adapter == null ? 0 : adapter.getDockedRowCount();
            scrollY = Integer.MAX_VALUE;
        }
        if (firstScrollableColumn >= columnCount || getMaxScrollX() - getActualScrollX() < 0) {
            firstScrollableColumn = adapter == null ? 0 : adapter.getDockedColumnCount();
            scrollX = Integer.MAX_VALUE;
        }

        setMeasuredDimension(w, h);
    }

    public int getActualScrollX() {
        return scrollX + sumArray(widths, dockedColumnCount, firstScrollableColumn);
    }

    public int getActualScrollY() {
        return scrollY + sumArray(heights, dockedRowCount, firstScrollableRow);
    }

    private int getMaxScrollX() {
        return Math.max(0, sumArray(widths) - width);
    }

    private int getMaxScrollY() {
        return Math.max(0, sumArray(heights) - height);
    }

    private int getFilledWidth() {
        List<View> rowViewList = getRowViewList();
        int columnSize = rowViewList.size();
        int firstRemovedRight = columnSize + firstScrollableColumn - dockedColumnCount;
        Log.d(FancyTable.class.getSimpleName(),
                String.format(
                        "firstScrollableColumn=%d, firstRemovedRight=%d",
                        firstScrollableColumn,
                        firstRemovedRight
                )
        );
        return sumArray(widths, 0, dockedColumnCount) + sumArray(widths, firstScrollableColumn, firstRemovedRight) - scrollX;
    }

    private int getFilledHeight() {
        int rowSize = bodyViewTable.size();
        return sumArray(heights, 0, dockedRowCount) + sumArray(heights, firstScrollableRow, rowSize + firstScrollableRow - dockedRowCount) - scrollY;
    }

    private void addLeft() {
        final int column = firstScrollableColumn - 1;
        final int index = dockedColumnCount;
        addLeftOrRight(column, index);
    }

    private void addTop() {
        final int row = firstScrollableRow - 1;
        final int index = dockedRowCount;
        addTopAndBottom(row, index);
    }

    private void addRight() {
        final int index = getRowViewList().size();
        final int column = firstScrollableColumn - dockedColumnCount + index;
        addLeftOrRight(column, index);
    }

    private void addBottom() {
        final int index = bodyViewTable.size();
        final int row = firstScrollableRow - dockedRowCount + index;
        addTopAndBottom(row, index);
    }

    private void addLeftOrRight(int column, int index) {

        View view;
        List<View> list;
        int row;

        for (row = 0; row < dockedRowCount; row++) {
            list = bodyViewTable.get(row);
            if (!adapter.isRowOneColumn(row)){
                view = makeView(row, column, widths[column], heights[row]);
                view.setTag(R.id.tag_column_type, null);
                list.add(index, view);
            }
        }

        for (row = firstScrollableRow; row < bodyViewTable.size() + firstScrollableRow - dockedRowCount; row++) {
            list = bodyViewTable.get(row - firstScrollableRow + dockedRowCount);
            if (!adapter.isRowOneColumn(row)){
                view = makeView(row, column, widths[column], heights[row]);
                view.setTag(R.id.tag_column_type, null);
                list.add(index, view);
            }
        }

    }

    private void addTopAndBottom(int row, int index) {
        View view;
        List<View> list = new ArrayList<>();
        List<View> rowViewList = getRowViewList();

        if (adapter.isRowOneColumn(row)){
            view = makeView(row, 0, width, heights[row]);
            view.setTag(R.id.tag_column_type, FILL_WIDTH_VIEW);
            list.add(view);
        } else {
            int column;

            for (column = 0; column < dockedColumnCount; column++) {
                view = makeView(row, column, widths[column], heights[row]);
                view.setTag(R.id.tag_column_type, null);
                list.add(view);
            }

            for (column = firstScrollableColumn; column < rowViewList.size() + firstScrollableColumn - dockedColumnCount; column++) {
                view = makeView(row, column, widths[column], heights[row]);
                view.setTag(R.id.tag_column_type, null);
                list.add(view);
            }
        }

        bodyViewTable.add(index, list);
    }

    private void removeLeft() {
        removeLeftOrRight(dockedColumnCount);
    }

    private void removeTop() {
        removeTopOrBottom(dockedRowCount);
    }

    private void removeRight() {
        List<View> rowViewList = getRowViewList();
        removeLeftOrRight(rowViewList.size() - 1);
    }

    private void removeBottom() {
        removeTopOrBottom(bodyViewTable.size() - 1);
    }

    private void removeLeftOrRight(int position) {
        for (List<View> list : bodyViewTable) {
            // shouldn't remove a header
            if (!(list.size() > 0 && FILL_WIDTH_VIEW.equals(list.get(0).getTag(R.id.tag_column_type)))){
                removeView(list.remove(position));
            }
        }
    }

    private void removeTopOrBottom(int position) {
        List<View> remove = bodyViewTable.remove(position);
        for (View view : remove) {
            removeView(view);
        }
    }

    private void repositionViews() {
        int left, top, right, bottom, row, column;

        int scrollStartLeft = sumArray(widths, 0, dockedColumnCount);
        int scrollStartTop = sumArray(heights, 0, dockedRowCount);
        List<View> columnCells;
        View view;


        // reposition fixed rows
        top = 0;
        for (row = 0; row < dockedRowCount && row < bodyViewTable.size(); row++) {
            bottom = top + heights[row];
            left = scrollStartLeft - scrollX;
            columnCells = bodyViewTable.get(row);
            if (adapter.isRowOneColumn(row)){
                view = columnCells.get(0);
                view.layout(0, top, width, bottom);
            } else {
                for (column = firstScrollableColumn; column < columnCells.size() + firstScrollableColumn - dockedColumnCount; column++) {
                    view = columnCells.get(column - firstScrollableColumn + dockedColumnCount);
                    right = left + widths[column];
                    view.layout(left, top, right, bottom);
                    Log.d(FancyTable.class.getSimpleName(),
                            String.format(
                                    "columnIndex=%d, left=%d, right=%d",
                                    column,
                                    left,
                                    right
                            )
                    );
                    left = right;
                }
            }
            top = bottom;
        }

        // reposition fixed columns
        top = scrollStartTop - scrollY;
        for (row = firstScrollableRow; row < bodyViewTable.size() + firstScrollableRow - dockedRowCount; row++) {
            bottom = top + heights[row];
            left = 0;
            columnCells = bodyViewTable.get(row - firstScrollableRow + dockedRowCount);
            if (adapter.isRowOneColumn(row)){
                view = columnCells.get(0);
                view.layout(0, top, width, bottom);
            } else {
                for (column = 0; column < dockedColumnCount && column < columnCells.size(); column++) {
                    view = columnCells.get(column);
                    right = left + widths[column];
                    view.layout(left, top, right, bottom);
                    left = right;
                }
            }
            top = bottom;
        }

        // reposition two way scrollable cells
        top = scrollStartTop - scrollY;
        for (row = firstScrollableRow; row < bodyViewTable.size() + firstScrollableRow - dockedRowCount; row++) {
            bottom = top + heights[row];
            left = scrollStartLeft - scrollX;
            columnCells = bodyViewTable.get(row - firstScrollableRow + dockedRowCount);
            if (adapter.isRowOneColumn(row)){
                view = columnCells.get(0);
                view.layout(0, top, width, bottom);
            } else {
                for (column = firstScrollableColumn; column < columnCells.size() + firstScrollableColumn - dockedColumnCount; column++) {
                    view = columnCells.get(column - firstScrollableColumn + dockedColumnCount);
                    right = left + widths[column];
                    view.layout(left, top, right, bottom);
                    left = right;
                }
            }
            top = bottom;
        }
        invalidate();
    }

    private int sumArray(int[] array) {
        return sumArray(array, 0, array.length);
    }

    private int sumArray(int[] array, int firstIndex, int length) {
        int sum = 0;
        for (int i = firstIndex; i < length; i++) {
            sum += array[i];
        }
        return sum;
    }

    private void scrollBounds() {
        scrollX = scrollBounds(scrollX, firstScrollableColumn, dockedColumnCount, widths, width);
        scrollY = scrollBounds(scrollY, firstScrollableRow, dockedRowCount, heights, height);
    }

    private int scrollBounds(int desiredScroll, int firstCell, int numFixedCells, int[] sizes, int viewSize) {
        Log.d(FancyTable.class.getSimpleName(),
                String.format(
                        "before desiredScroll=%d",
                        desiredScroll
                )
        );
        if (desiredScroll == 0) {
            // no op
        } else if (desiredScroll < 0) {
            desiredScroll = Math.max(desiredScroll, -sumArray(sizes, numFixedCells, firstCell));
        } else {

			/*desiredScroll = Math.min(desiredScroll, Math.max(0,
					sumArray(sizes, firstCell + numFixedCells, sizes.length - numFixedCells - firstCell) + sumArray(sizes, 0, numFixedCells) - viewSize
					)
			);*/
            desiredScroll = Math.min(desiredScroll, Math.max(0,
                    sumArray(sizes, firstCell, sizes.length) + sumArray(sizes, 0, numFixedCells) - viewSize
                    )
            );
        }
        Log.d(FancyTable.class.getSimpleName(),
                String.format(
                        "after desiredScroll=%d",
                        desiredScroll
                )
        );
        return desiredScroll;
    }

    private void adjustFirstCellsAndScroll() {
        int[] values;

        values = adjustFirstCellsAndScroll(scrollX, firstScrollableColumn, widths);
        scrollX = values[0];
        firstScrollableColumn = values[1];

        values = adjustFirstCellsAndScroll(scrollY, firstScrollableRow, heights);
        scrollY = values[0];
        firstScrollableRow = values[1];
    }

    private int[] adjustFirstCellsAndScroll(int scroll, int firstCell, int[] sizes) {
        if (scroll == 0) {
            // no op
        } else if (scroll > 0) {
            while (sizes[firstCell + 1] < scroll) {
                firstCell++;
                scroll -= sizes[firstCell];
            }
        } else {
            while (scroll < 0) {
                scroll += sizes[firstCell];
                firstCell--;
            }
        }
        return new int[]{scroll, firstCell};
    }

    private void shadowsVisibility() {
        final int actualScrollX = getActualScrollX();
        final int actualScrollY = getActualScrollY();
        final int[] remainPixels = {
                actualScrollX, actualScrollY, getMaxScrollX() - actualScrollX, getMaxScrollY() - actualScrollY,
        };

        for (int i = 0; i < shadows.length; i++) {
            setAlpha(shadows[i], Math.min(remainPixels[i] / (float) shadowSize, 1));
        }
    }

    private void setAlpha(ImageView imageView, float alpha) {
        imageView.setAlpha(alpha);
    }

    private void addShadow(ImageView imageView, int l, int t, int r, int b) {
        imageView.layout(l, t, r, b);
        addView(imageView);
    }

    private void resetTable() {
        bodyViewTable.clear();

        removeAllViews();
    }

    private View makeAndSetup(int row, int column, int left, int top, int right, int bottom) {
        final View view = makeView(row, column, right - left, bottom - top);
        view.layout(left, top, right, bottom);
        return view;
    }

    private View makeView(int row, int column, int w, int h) {
        final int itemViewType = adapter.getItemViewType(row, column);
        final View recycledView;
        if (itemViewType == FancyTableAdapter.IGNORE_ITEM_VIEW_TYPE) {
            recycledView = null;
        } else {
            recycledView = recycler.getRecycledView(itemViewType);
        }
        final View view = adapter.getView(row, column, recycledView, this);
        view.setTag(R.id.tag_type_view, itemViewType);
        view.setTag(R.id.tag_row, row);
        view.setTag(R.id.tag_column, column);
        view.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
        addTableView(view, row, column);
        return view;
    }

    private void addTableView(View view, int row, int column) {
        if (row < dockedRowCount && column < dockedColumnCount){
            addView(view);
        } else if(row < dockedRowCount || column < dockedColumnCount && !adapter.isRowOneColumn(row)){
            int shadows = 0;
            if (adapter.isColumnShadowShown()) shadows++;
            if (adapter.isRowShadowShown()) shadows++;
            int index = getChildCount() - (dockedRowCount * dockedColumnCount) - shadows;
            addView(view, index < 0 ? 0 : index);
        }else {
            addView(view, 0);
        }
    }

    private class TableAdapterDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            needRelayout = true;
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            // Do nothing
        }
    }

    // http://stackoverflow.com/a/6219382/842697
    private class Flinger implements Runnable {

        private final Scroller scroller;

        private int lastX = 0;
        private int lastY = 0;

        Flinger(Context context) {
            scroller = new Scroller(context);
        }

        void start(int initX, int initY, int initialVelocityX, int initialVelocityY, int maxX, int maxY) {
            scroller.fling(initX, initY, initialVelocityX, initialVelocityY, 0, maxX, 0, maxY);

            lastX = initX;
            lastY = initY;
            post(this);
        }

        public void run() {
            if (scroller.isFinished()) {
                return;
            }

            boolean more = scroller.computeScrollOffset();
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();
            int diffX = lastX - x;
            int diffY = lastY - y;
            if (diffX != 0 || diffY != 0) {
                scrollBy(diffX, diffY);
                lastX = x;
                lastY = y;
            }

            if (more) {
                post(this);
            }
        }

        boolean isFinished() {
            return scroller.isFinished();
        }

        void forceFinished() {
            if (!scroller.isFinished()) {
                scroller.forceFinished(true);
            }
        }
    }
}
