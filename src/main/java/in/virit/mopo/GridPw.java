package in.virit.mopo;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * A helper class to work with the vaadin-grid component.
 */
public class GridPw {

    private final Locator root;

    /**
     * Creates a Grid page object for the given grid locator.
     *
     * @param gridLocator the Playwright locator for the grid
     */
    public GridPw(Locator gridLocator) {
        this.root = gridLocator;
    }

    /**
     * Creates a Grid page object for the first grid on the page.
     *
     * @param page the Playwright page
     */
    public GridPw(Page page) {
        this(page.locator("vaadin-grid"));
    }

    /**
     * Gets the total number of rows.
     *
     * @return the number of rows
     */
    public int getRenderedRowCount() {
        Integer evaluate = (Integer) root.elementHandle().evaluate("e => e._getRenderedRows().length");
        return evaluate;
    }

    /**
     * Returns the index of the first visible row
     *
     * @return the index.
     */
    public int getFirstVisibleRowIndex() {
        return (Integer) root.elementHandle().evaluate("e => e._firstVisibleIndex");
    }

    /**
     * Scrolls the grid to the given index.
     *
     * @param index the row index to scroll to
     */
    public void scrollToIndex(int index) {
        root.elementHandle().evaluate("e => e.scrollToIndex(" + index + ")");

        // FIXME this don't seem to be stable, but
        // 10*100ms timeout seems to do the trick most of the time
        int value = (Integer) root.evaluate("""
                g => {
                    return new Promise((resolve, reject) => {
                        var x = 0;
                        const isDoneLoading = () => {
                            return !g.$connector.hasRootRequestQueue()
                        };
                        
                        const isVaadinConnectionActive = () => {
                            if (window.Vaadin && window.Vaadin.Flow && window.Vaadin.Flow.clients) {
                              var clients = window.Vaadin.Flow.clients;
                              for (var client in clients) {
                                if (clients[client].isActive()) {
                                  return false;
                                }
                              }
                              return true;
                            } else if (window.Vaadin && window.Vaadin.Flow && window.Vaadin.Flow.devServerIsNotLoaded) {
                              return false;
                            } else {
                              return true;
                            }
                        };
                        
                        if (isDoneLoading() && !isVaadinConnectionActive()) {
                            resolve(x);
                            return;
                        }
                        
                        var intervalID = window.setInterval(function () {
                            if (isDoneLoading() && !isVaadinConnectionActive()) {
                                window.clearInterval(intervalID);
                                resolve(x+1);
                            } else {
                               if (++x === 10) {
                                   window.clearInterval(intervalID);
                                   resolve(-1);
                               }
                           }
                        }, 100);
                    });
                }""");
        // System.out.println("RETURN value = " + value);
    }

    /**
     * Selects the given row.
     *
     * @param rowIndex the row index to select
     */
    public void selectRow(int rowIndex) {
        String script = """
                grid => {
                    var firstRowIndex = %s;
                    var lastRowIndex = firstRowIndex;
                    var rowsInDom = grid._getRenderedRows();
                    var rows = Array.from(rowsInDom).filter((row) => { return row.index >= firstRowIndex && row.index <= lastRowIndex;});
                    var row = rows[0];
                    grid.activeItem = row._item;
                }
                """.formatted(rowIndex);
        root.elementHandle().evaluate(script);
    }

    /**
     * Returns a RowPw helper representing the row defined by the given index.
     *
     * @param rowIndex the row index
     * @return the RowPw for editing the UI state or to get cell locators for
     * assertions.
     */
    public RowPw getRow(int rowIndex) {
        if (!isRowInView(rowIndex)) {
            scrollToIndex(rowIndex);
        }
        return new RowPw(rowIndex);
    }

    /**
     * Checks if the given row is in the visible viewport.
     *
     * @param rowIndex the row to check
     * @return <code>true</code> if the row is at least partially in view,
     * <code>false</code> otherwise
     */
    public boolean isRowInView(int rowIndex) {
        return (getFirstVisibleRowIndex() <= rowIndex
                && rowIndex <= getLastVisibleRowIndex());
    }

    /**
     * Returns the index of last visible row.
     *
     * @return the index
     */
    public int getLastVisibleRowIndex() {
        return (Integer) root.elementHandle().evaluate("e => e._lastVisibleIndex");
    }

    /**
     * Represents a row in the vaadin-grid component. Not that there is no DOM
     * element backing this row, but this is purely virtual helper class based
     * on row index.
     */
    public class RowPw {

        private final int rowIndex;

        private RowPw(int rowIndex) {
            this.rowIndex = rowIndex;
        }

        /**
         * Gets the cell locator at the given index.
         *
         * @param cellIndex the cell index (0-based, unlike the CSS nth-child
         * selector, whose designer should be hung by the balls, in case they
         * have any)
         * @return the cell locator
         */
        public Locator getCell(int cellIndex) {
            int indexInVirtualTable = (Integer) root.evaluate("g => g._getRenderedRows().indexOf(g._getRenderedRows().filter(r => r.index == %s)[0]);".formatted(rowIndex));
            indexInVirtualTable += 1; // 1-based :-)
            String name = root.locator("#items tr:nth-child(%s) td:nth-child(%s) slot".formatted(indexInVirtualTable, cellIndex + 1))
                    .getAttribute("name");
            return root.locator("vaadin-grid-cell-content[slot='%s']".formatted(name));
        }

        /**
         * Gets the cell with the given header text.
         *
         * @param headerText the header text
         * @return the cell locator
         */
        public Locator getCell(String headerText) {
            // this depends heavily on Grid's internal implementation
            // Grid developers probably have a better way to do this
            String slot = root.locator("vaadin-grid-cell-content")
                    .filter(new Locator.FilterOptions().setHasText(headerText))
                    .getAttribute("slot");
            String substring = slot.substring(slot.lastIndexOf("-") + 1);
            int cellIndex = Integer.parseInt(substring);
            return getCell(cellIndex);
        }

        /**
         * Selects the given row.
         */
        public void select() {
            GridPw.this.selectRow(rowIndex);
        }
    }

}
