package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;

public class UtcDateConverter extends AbstractConverter {
	
	private static final UtcDateConverter instance = new UtcDateConverter();

	private DateTimeFormatter format;

	private UtcDateConverter() {
		format = new DateTimeFormatterBuilder().appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
				.appendOptional(new DateTimeFormatterBuilder().appendLiteral("T").toFormatter())
                .appendOptional(new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter())
                .appendOptional(new DateTimeFormatterBuilder().appendZoneOrOffsetId().toFormatter())
				.appendOptional(new DateTimeFormatterBuilder().appendLiteral('[').parseCaseSensitive()
						.appendZoneRegionId().appendLiteral(']').toFormatter()).toFormatter().withZone(ZoneId.systemDefault());
	}

	public static JsonValueConverter converter() {
		return instance;
	}

	public static DateParser dateParser() {
		return ISODateParser.parser();
	}

	private DateTimeFormatter getFormat() {
		return format;
	}

	@Override
	protected Object valueNonNull(JsonNode node) {

		Temporal temporal = ISODateParser.parser().fromString(node.asText());

		GregorianCalendar calendar = new GregorianCalendar();

		ZoneId zone = temporal.query(TemporalQueries.zone());
		if (zone != null) {
			calendar.setTimeZone(TimeZone.getTimeZone(zone));
		}

		if (temporal.isSupported(ChronoField.YEAR)) {
			int year = temporal.get(ChronoField.YEAR);
			int monthOfYear = temporal.get(ChronoField.MONTH_OF_YEAR);
			int dayOfMonth = temporal.get(ChronoField.DAY_OF_MONTH);
			calendar.set(year, --monthOfYear, dayOfMonth);
		}

		if (temporal.isSupported(ChronoField.HOUR_OF_DAY)) {
			int hours = temporal.get(ChronoField.HOUR_OF_DAY);
			int minutes = temporal.get(ChronoField.MINUTE_OF_HOUR);
			int seconds = temporal.get(ChronoField.SECOND_OF_MINUTE);
			calendar.set(Calendar.HOUR_OF_DAY, hours);
			calendar.set(Calendar.MINUTE, minutes);
			calendar.set(Calendar.SECOND, seconds);
		}

		if (temporal.isSupported(ChronoField.MILLI_OF_SECOND)) {
			int millis = temporal.get(ChronoField.MILLI_OF_SECOND);
			calendar.setTimeInMillis(calendar.getTimeInMillis() + millis);
		}

		return calendar.getTime();
	}

	public interface DateParser {
		Temporal fromString(String s);
	}

	private static class ISODateParser implements DateParser {

		private static final ISODateParser parser = new ISODateParser();

		public static DateParser parser() {
			return parser;
		}

		@Override
		public Temporal fromString(String s) {
			return fromParsed(instance.getFormat().parse(s));
		}

		Temporal fromParsed(TemporalAccessor parsed) {

			Optional<ZonedDateTime> zonedDateTime = getZonedDateTime(parsed);
			if (zonedDateTime.isPresent()) {
				return zonedDateTime.get();
			}

			Optional<LocalDateTime> localDateTime = getLocalDateTime(parsed);
			if (localDateTime.isPresent()) {
				return localDateTime.get();
			}

			Optional<LocalDate> localDate = getLocalDate(parsed);
			if (localDate.isPresent()) {
				return localDate.get();
			}

			Optional<LocalTime> localTime = getLocalTime(parsed);
			if (localTime.isPresent()) {
				return localTime.get();
			}

			throw new LinkRestException(Response.Status.BAD_REQUEST, "Failed to build date/time/datetime: " + parsed);
		}

		private Optional<ZonedDateTime> getZonedDateTime(TemporalAccessor parsed) {

			ZoneId zone = parsed.query(TemporalQueries.zone());
			if (zone == null) {
				return Optional.empty();
			}

			Optional<ZonedDateTime> zonedDateTime = getLocalDateTime(parsed)
					.map(localDateTime -> localDateTime.atZone(zone));

			if (!zonedDateTime.isPresent()) {
				zonedDateTime = getLocalDate(parsed).map(localDate -> localDate.atStartOfDay(zone));
			}
			return zonedDateTime;
		}

		private Optional<LocalDateTime> getLocalDateTime(TemporalAccessor parsed) {
			return getLocalDate(parsed).map(date -> {
				Optional<LocalTime> time = getLocalTime(parsed);
				return time.isPresent()? date.atTime(time.get()) : null;
			});
		}

		private Optional<LocalDate> getLocalDate(TemporalAccessor parsed) {
			return Optional.ofNullable(parsed.query(TemporalQueries.localDate()));
		}

		private Optional<LocalTime> getLocalTime(TemporalAccessor parsed) {
			return Optional.ofNullable(parsed.query(TemporalQueries.localTime()));
		}
	}
}
