package org.peakaboo.framework.swidget.widgets.filechooser.breadcrumb;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.peakaboo.framework.swidget.widgets.breadcrumb.BreadCrumb;
import org.peakaboo.framework.swidget.widgets.filechooser.places.Place;
import org.peakaboo.framework.swidget.widgets.filechooser.places.Places;

public class FileBreadCrumb extends BreadCrumb<File> {

	public FileBreadCrumb(JFileChooser chooser) {
		super(FileBreadCrumb::format, f -> {
			if (!f.equals(chooser.getCurrentDirectory())) {
				chooser.setCurrentDirectory(f);
			}
		});
		
		this.setAlignment(BorderLayout.LINE_START);
		
		setEntryBuilder(item -> new FileBreadCrumbEntry(this, item, FileBreadCrumb::format));
		
		chooser.addPropertyChangeListener(l -> {
			File dir = chooser.getCurrentDirectory();
			this.setFile(dir);
		});
		
	}
	

	
	private static String format(File f) {
		Place place = Places.forPlatform().get(f);
		if (place != null && place.isRoot()) {
			return place.getName();
		} else {
			return FileSystemView.getFileSystemView().getSystemDisplayName(f);
		}
	}
	
	public void setFile(File f) {
		if (!f.isDirectory()) {
			f = f.getParentFile();
		}
		
		//If this File is already in the breadcrumb, don't reload everything, just change the selection
		if (contains(f)) {
			setSelected(f);
			return;
		}
		
		List<File> items = new ArrayList<>();
		File p = f;
		Places places = Places.forPlatform();
		while (p != null) {
			items.add(0, p);
			if (places.has(p) && places.get(p).isRoot()) {
				break;
			}
			p = FileSystemView.getFileSystemView().getParentDirectory(p);
		}
		this.setAll(items, f);
	}
	

}
