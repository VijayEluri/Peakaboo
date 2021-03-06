package org.peakaboo.framework.swidget.widgets.breadcrumb;

import static java.util.stream.Collectors.toList;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.peakaboo.framework.stratus.controls.ButtonLinker;
import org.peakaboo.framework.swidget.icons.IconFactory;
import org.peakaboo.framework.swidget.widgets.buttons.ImageButton;
import org.peakaboo.framework.swidget.widgets.buttons.ImageButtonSize;

public class BreadCrumb<T> extends JPanel {

	private ButtonLinker linker;
	private Function<T, String> formatter;
	private List<T> items;
	private int selected;
	private int first, last, requested=-1; //first and last indexes shown
	private List<BreadCrumbEntry<T>> entries = new ArrayList<>();
	
	private Consumer<T> onSelect;
	private String alignment = BorderLayout.CENTER;
	
	private ImageButton goLeft, goRight;

	private Function<T, BreadCrumbEntry<T>> entryBuilder;
	
	MouseWheelListener onScroll = e -> {
		int wheel = e.getWheelRotation();
		if (wheel <= -1) {
			doGoLeft();
		} else if (wheel >= 1) {
			doGoRight();
		}
	};
	
	
	
	
	public BreadCrumb(Consumer<T> onSelect) {
		this(Object::toString, onSelect);
	}
	
	public BreadCrumb(Function<T, String> formatter, Consumer<T> onSelect) {
		this.formatter = formatter;
		this.onSelect = onSelect;
		
		goLeft  = new ImageButton()
				.withButtonSize(ImageButtonSize.COMPACT)
				.withAction(this::doGoLeft);
		goLeft.setIcon(new ImageIcon(BreadCrumb.class.getClassLoader().getResource("swidget/widgets/breadcrumb/arrow-left.png")));
		goRight = new ImageButton()
				.withButtonSize(ImageButtonSize.COMPACT)
				.withAction(this::doGoRight);
		goRight.setIcon(new ImageIcon(BreadCrumb.class.getClassLoader().getResource("swidget/widgets/breadcrumb/arrow-right.png")));

		entryBuilder = item -> new BreadCrumbEntry<>(this, item, formatter);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				make();
			}
		});

		
		setOpaque(false);
		
	}
	
	
	/**
	 * Sets the function for building a {@link BreadCrumbEntry} 
	 */
	public void setEntryBuilder(Function<T, BreadCrumbEntry<T>> entryBuilder) {
		this.entryBuilder = entryBuilder;
	}

	/**
	 * Indicates if there are more entries to the left/up of the current range
	 */
	private boolean canGoLeft() {
		return first > 0;
	}

	/**
	 * Expands the range of visible entries left/up by one
	 */
	private void doGoLeft() {
		if (!canGoLeft()) {
			return;
		}
		requested = Math.max(0, first-1);
		make();
	}
	
	/**
	 * Indicates if there are more entries to the right/down of the current range
	 */
	private boolean canGoRight() {
		return last < entries.size();
	}
	
	/**
	 * Expands the range of visible entries right/down by one
	 */
	private void doGoRight() {
		if (!canGoRight()) {
			return;
		}
		requested = Math.min(entries.size(), last);
		make();
	}

	/**
	 * Sets the items shown in the breadcrumb
	 */
	public void setItems(List<T> items) {
		setItemsInner(items);
		make();
	}
	/**
	 * Sets the items shown in the breadcrumb, but does not rebuild the UI
	 */
	private void setItemsInner(List<T> items) {
		this.items = items;
		this.requested = -1;
		entries = new ArrayList<>();
		for (T item : items) {
			entries.add(entryBuilder.apply(item));
		}
	}
	
	/**
	 * Sets the selected item in the breadcrumb
	 */
	public void setSelected(T item) {
		if (getSelected().equals(item)) {
			return;
		}
		setSelectedInner(item);
		make();
	}
	/**
	 * Sets the selected item in the breadcrumb, but does not rebuild the UI
	 */
	private void setSelectedInner(T item) {
		if (!items.contains(item)) {
			throw new RuntimeException("Selected item must belong to item list");
		}
		this.selected = index(item);
		for (BreadCrumbEntry<T> entry : entries) {
			entry.onSelection(item);
		}
	}
	
	/**
	 * Sets the items in the breadcrumb and indicates which one should be selected
	 * @param items
	 * @param selected
	 */
	public void setAll(List<T> items, T selected) {
		setItemsInner(items);
		setSelectedInner(selected);
		make();
	}
	
	/**
	 * Used by button actions to change the selection
	 */
	protected void makeSelection(T item) {
		if (!items.contains(item)) {
			throw new RuntimeException("Selected item must belong to item list");
		}
		this.selected = index(item);
		onSelect.accept(item);
	}
	
	/**
	 * Gets the selected item
	 */
	public T getSelected() {
		if (entries.isEmpty()) {
			return null;
		}
		if (entries.size() <= selected) {
			return null;
		}
		return entries.get(selected).getItem();
	}
	
	
	/**
	 * Gets the {@link BorderLayout} alignment
	 */
	public String getAlignment() {
		return alignment;
	}

	/**
	 * Sets the breadcrumb's {@link BorderLayout} alignment
	 */
	public void setAlignment(String alignment) {
		this.alignment = alignment;
		make();
	}

	/**
	 * Clears the items from this breadcrumb
	 */
	public void clear() {
		items = null;
		entries = new ArrayList<>();
		selected = 0;
		requested = 0;
		make();
	}
	
	/**
	 * Rebuilds the UI based on current state
	 */
	private void make() {
		this.removeAll();
		setLayout(new BorderLayout());	
			
		ButtonGroup group = new ButtonGroup();

		if (entries.size() == 0) {
			return;
		}
		
		int width = getWidth();
		
		for (BreadCrumbEntry<T> entry : entries) {
			group.add(entry.getButton());
		}

		boolean requestedUp = false;
		boolean requestedDown = false;
		if (requested == -1) {
			first = selected;
			last = selected+1;
		} else {
			first = requested;
			last = requested+1;
			requestedUp = requested < selected;
			requestedDown = requested > selected;			
		}
		
		//grow the list towards the selected item
		if (requestedUp) {
			while (last <= selected && last < entries.size()) {
				int needed = entriesWidth(subset(first, last+1));
				if (needed < width) {
					last++;
				} else {
					break;
				}
			}
		} else if (requestedDown) {
			while (first > selected && first > 0) {
				int needed = entriesWidth(subset(first-1, last));
				if (needed < width) {
					first--;
				} else {
					break;
				}
			}
		}
		
		//grow the list downwards toward the end
		while (canGoRight() && !requestedDown) {
			int needed = entriesWidth(subset(first, last+1));
			if (needed < width) {
				last++;
			} else {
				break;
			}
		}
		
		//grow the list upwards towards the start
		while (canGoLeft() && !requestedUp) {
			int needed = entriesWidth(subset(first-1, last));
			if (needed < width) {
				first--;
			} else {
				break;
			}
		}
		
		goLeft.setEnabled(canGoLeft());
		goRight.setEnabled(canGoRight());
		
		List<AbstractButton> buttons = new ArrayList<>();
		buttons.add(goLeft);
		buttons.addAll(subset(first, last).stream().map(e -> e.getButton()).collect(toList()));
		buttons.add(goRight);
		ButtonLinker linker = new ButtonLinker(buttons, true);
		this.add(linker, this.alignment);
		
		this.repaint();
		
	}
	
	/**
	 * Returns a subset of the entries based on the given range
	 */
	private List<BreadCrumbEntry<T>> subset(int start, int end) {
		return entries.subList(start, end);
	}

	/**
	 * Calculates the required width of the given entries
	 */
	private int entriesWidth(List<BreadCrumbEntry<T>> entries) {
		int fixed = goLeft.getPreferredSize().width + goRight.getPreferredSize().width;
		return entries.stream().map(b -> b.width()).reduce(fixed, (a, b) -> a+b);
	}

	/**
	 * Determines if the given item is in the current set of items
	 */
	protected boolean contains(T item) {
		return index(item) >= 0;
	}
	
	/**
	 * Returns the index of the given item in the list of items
	 */
	private int index(T item) {
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getItem().equals(item)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the last entry in the list of items
	 */
	protected T tail() {
		if (entries.size() == 0) { return null; }
		return entries.get(entries.size()-1).getItem();
	}

}
