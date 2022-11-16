
package dev.badbird.griefpreventiontp.menus;

import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.components.util.Legacy;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import lombok.SneakyThrows;
import net.badbird5907.blib.objects.TypeCallback;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * GUI that allows you to have multiple pages
 */
@SuppressWarnings("unused")
public class StreamedPaginatedGui extends BaseGui {
    // Saves the current page items and it's slot
    private final Map<Integer, GuiItem> currentPage;

    private int pageSize;
    private int pageNum = 1;

    private TypeCallback<Integer, StreamedPaginatedGui> pageCallback;
    private TypeCallback<List<GuiItem>, Integer> populateCallback;

    /**
     * Main constructor to provide a way to create PaginatedGui
     *
     * @param rows                 The amount of rows the GUI should have
     * @param pageSize             The page size.
     * @param title                The GUI's title using {@link String}
     * @param interactionModifiers A set containing what {@link InteractionModifier} this GUI should have
     * @author SecretX
     * @since 3.0.3
     */
    public StreamedPaginatedGui(final int rows,
                                final int pageSize,
                                @NotNull final String title,
                                @NotNull final Set<InteractionModifier> interactionModifiers,
                                @NotNull TypeCallback<Integer, StreamedPaginatedGui> pageCallback, // Callback to compute the number of pages,
                                @NotNull TypeCallback<List<GuiItem>, Integer> populateCallback // Callback to populate the page
                                ) {
        super(rows, title, interactionModifiers);
        this.pageSize = pageSize;
        int inventorySize = rows * 9;
        this.currentPage = new LinkedHashMap<>(inventorySize);
        this.pageCallback = pageCallback;
        this.populateCallback = populateCallback;
    }
    public StreamedPaginatedGui(final int rows,
                                final int pageSize,
                                @NotNull final Component title,
                                @NotNull final Set<InteractionModifier> interactionModifiers,
                                @NotNull TypeCallback<Integer, StreamedPaginatedGui> pageCallback, // Callback to compute the number of pages,
                                @NotNull TypeCallback<List<GuiItem>, Integer> populateCallback // Callback to populate the page
                                ) {
        this(rows, pageSize, Legacy.SERIALIZER.serialize(title), interactionModifiers, pageCallback, populateCallback);
    }

    /**
     * Sets the page size
     *
     * @param pageSize The new page size
     * @return The GUI for easier use when declaring, works like a builder
     */
    public BaseGui setPageSize(final int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @SneakyThrows
    private void populate() {
        Method method = BaseGui.class.getDeclaredMethod("populateGui");
        method.setAccessible(true);
        method.invoke(this);
    }

    /**
     * Overridden {@link BaseGui#update()} to use the paginated open
     */
    @Override
    public void update() {
        getInventory().clear();
        populate();

        updatePage();
    }

    /**
     * Updates the page {@link GuiItem} on the slot in the page
     * Can get the slot from {@link InventoryClickEvent#getSlot()}
     *
     * @param slot      The slot of the item to update
     * @param itemStack The new {@link ItemStack}
     */
    public void updatePageItem(final int slot, @NotNull final ItemStack itemStack) {
        if (!currentPage.containsKey(slot)) return;
        final GuiItem guiItem = currentPage.get(slot);
        guiItem.setItemStack(itemStack);
        getInventory().setItem(slot, guiItem.getItemStack());
    }

    public int getSlotFromRowColWrap(int row, int col) {
        return (col + (row - 1) * 9) - 1;
    }

    /**
     * Alternative {@link #updatePageItem(int, ItemStack)} that uses <i>ROWS</i> and <i>COLUMNS</i> instead
     *
     * @param row       The row of the slot
     * @param col       The columns of the slot
     * @param itemStack The new {@link ItemStack}
     */
    public void updatePageItem(final int row, final int col, @NotNull final ItemStack itemStack) {
        updateItem(getSlotFromRowColWrap(row, col), itemStack);
    }

    /**
     * Overrides {@link BaseGui#open(HumanEntity)} to use the paginated populator instead
     *
     * @param player The {@link HumanEntity} to open the GUI to
     */
    @Override
    public void open(@NotNull final HumanEntity player) {
        open(player, 1);
    }

    /**
     * Specific open method for the Paginated GUI
     * Uses {@link #populatePage()}
     *
     * @param player   The {@link HumanEntity} to open it to
     * @param openPage The specific page to open at
     */
    public void open(@NotNull final HumanEntity player, final int openPage) {
        if (player.isSleeping()) return;
        if (openPage <= getPagesNum() || openPage > 0) pageNum = openPage;

        getInventory().clear();
        currentPage.clear();

        populate();

        if (pageSize == 0) pageSize = calculatePageSize();

        populatePage();

        player.openInventory(getInventory());
    }

    /**
     * Overrides {@link BaseGui#updateTitle(String)} to use the paginated populator instead
     * Updates the title of the GUI
     * <i>This method may cause LAG if used on a loop</i>
     *
     * @param title The title to set
     * @return The GUI for easier use when declaring, works like a builder
     */
    @Override
    @NotNull
    public BaseGui updateTitle(@NotNull final String title) {
        setUpdating(true);

        final List<HumanEntity> viewers = new ArrayList<>(getInventory().getViewers());

        setInventory(Bukkit.createInventory(this, getInventory().getSize(), title));

        for (final HumanEntity player : viewers) {
            open(player, getPageNum());
        }

        setUpdating(false);

        return this;
    }

    /**
     * Gets an immutable {@link Map} with all the current pages items
     *
     * @return The {@link Map} with all the {@link #currentPage}
     */
    @NotNull
    public Map<@NotNull Integer, @NotNull GuiItem> getCurrentPageItems() {
        return Collections.unmodifiableMap(currentPage);
    }


    /**
     * Gets the current page number
     *
     * @return The current page number
     */
    public int getCurrentPageNum() {
        return pageNum;
    }

    /**
     * Gets the next page number
     *
     * @return The next page number or {@link #pageNum} if no next is present
     */
    public int getNextPageNum() {
        if (pageNum + 1 > getPagesNum()) return pageNum;
        return pageNum + 1;
    }

    /**
     * Gets the previous page number
     *
     * @return The previous page number or {@link #pageNum} if no previous is present
     */
    public int getPrevPageNum() {
        if (pageNum - 1 == 0) return pageNum;
        return pageNum - 1;
    }

    /**
     * Goes to the next page
     *
     * @return False if there is no next page.
     */
    public boolean next() {
        if (pageNum + 1 > getPagesNum()) return false;

        pageNum++;
        updatePage();
        return true;
    }

    /**
     * Goes to the previous page if possible
     *
     * @return False if there is no previous page.
     */
    public boolean previous() {
        if (pageNum - 1 == 0) return false;

        pageNum--;
        updatePage();
        return true;
    }

    /**
     * Gets the page item for the GUI listener
     *
     * @param slot The slot to get
     * @return The GuiItem on that slot
     */
    GuiItem getPageItem(final int slot) {
        return currentPage.get(slot);
    }

    /**
     * Gets the items in the page
     *
     * @param givenPage The page to get
     * @return A list with all the page items
     */
    private List<GuiItem> getPageNum(final int givenPage) {
        final int page = givenPage - 1;

        /*
        final List<GuiItem> guiPage = new ArrayList<>();

        int max = ((page * pageSize) + pageSize);
        if (max > pageItems.size()) max = pageItems.size();

        for (int i = page * pageSize; i < max; i++) {
            guiPage.add(pageItems.get(i));
        }

        return guiPage;
         */
        return populateCallback.callback(page);
    }

    /**
     * Gets the number of pages the GUI has
     *
     * @return The pages number
     */
    public int getPagesNum() {
        //return (int) Math.ceil((double) pageItems.size() / pageSize);
        return pageCallback.callback(this);
    }

    /**
     * Populates the inventory with the page items
     */
    private void populatePage() {
        // Adds the paginated items to the page
        for (final GuiItem guiItem : getPageNum(pageNum)) {
            for (int slot = 0; slot < getRows() * 9; slot++) {
                if (getGuiItem(slot) != null || getInventory().getItem(slot) != null) continue;
                currentPage.put(slot, guiItem);
                getInventory().setItem(slot, guiItem.getItemStack());
                break;
            }
        }
    }

    /**
     * Gets the current page items to be used on other gui types
     *
     * @return The {@link Map} with all the {@link #currentPage}
     */
    Map<Integer, GuiItem> getMutableCurrentPageItems() {
        return currentPage;
    }

    /**
     * Clears the page content
     */
    void clearPage() {
        for (Map.Entry<Integer, GuiItem> entry : currentPage.entrySet()) {
            getInventory().setItem(entry.getKey(), null);
        }
    }

    /**
     * Gets the page size
     *
     * @return The page size
     */
    int getPageSize() {
        return pageSize;
    }

    /**
     * Gets the page number
     *
     * @return The current page number
     */
    int getPageNum() {
        return pageNum;
    }

    /**
     * Sets the page number
     *
     * @param pageNum Sets the current page to be the specified number
     */
    void setPageNum(final int pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * Updates the page content
     */
    void updatePage() {
        clearPage();
        populatePage();
    }

    /**
     * Calculates the size of the give page
     *
     * @return The page size
     */
    int calculatePageSize() {
        int counter = 0;

        for (int slot = 0; slot < getRows() * 9; slot++) {
            if (getInventory().getItem(slot) == null) counter++;
        }

        return counter;
    }

}
