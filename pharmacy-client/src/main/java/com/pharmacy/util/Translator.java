package com.pharmacy.util;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Translator {

	private static final Logger LOG = LoggerFactory.getLogger(Translator.class);

	private static final String BUNDLE_BASE_NAME = "i18n.messages";

	private static Translator instance;

	private volatile ResourceBundle resourceBundle;

	private volatile Locale currentLocale;

	private final List<Consumer<Locale>> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Private constructor to prevent instantiation from outside. Initializes with
	 * the default locale (English).
	 */
	private Translator() {
		setLocale(new Locale("vi", "VN"));
	}

	public static synchronized Translator getInstance() {
		if (instance == null)
			instance = new Translator();

		return instance;
	}

	public Locale getCurrentLocale() {
		return currentLocale;
	}

	/**
	 * Sets the application's locale and loads the corresponding resource bundle.
	 * Notifies all registered listeners about the change.
	 *
	 * @param locale The new locale to set.
	 */
	public synchronized void setLocale(Locale locale) {
		if (locale == null)
			throw new IllegalArgumentException("locale must not be null");

		try {
			ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
			this.resourceBundle = bundle;
			this.currentLocale = locale;
			fireLanguageChangeEvent(locale);
		} catch (MissingResourceException e) {
			LOG.warn("Resource bundle not found for locale {}, falling back to English", locale);
			try {
				this.resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.ENGLISH);
				this.currentLocale = Locale.ENGLISH;
				fireLanguageChangeEvent(Locale.ENGLISH);
			} catch (MissingResourceException ex) {
				LOG.error("English resource bundle also missing", ex);
				this.resourceBundle = null;
			}
		}
	}

	public String getString(String key) {
		if (key == null)
			return "!!null-key!!";

		ResourceBundle rb = this.resourceBundle;
		if (rb != null && rb.containsKey(key))
			return rb.getString(key);

		return "!" + key + "!";
	}

	public void addLanguageChangeListener(Consumer<Locale> listener) {
		if (listener != null)
			listeners.add(listener);
	}

	public void removeLanguageChangeListener(Consumer<Locale> listener) {
		listeners.remove(listener);
	}

	private void fireLanguageChangeEvent(Locale newLocale) {
		for (Consumer<Locale> listener : listeners) {
			try {
				listener.accept(newLocale);
			} catch (Exception e) {
				LOG.warn("Language change listener threw an exception", e);
			}
		}
	}

	public void clearListeners() {
		listeners.clear();
	}
}