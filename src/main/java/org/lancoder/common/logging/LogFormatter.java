package org.lancoder.common.logging;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Custom logging formatter for lancoder without throwing support
 *
 * @author justin
 *
 */
public  class LogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        sb.append(new Date(record.getMillis()))
            .append(" ")
            .append(record.getLevel())
            .append(": ")
            .append(formatMessage(record));

        return sb.toString();
    }

}
