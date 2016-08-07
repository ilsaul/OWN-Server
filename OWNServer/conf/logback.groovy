// Config LogBack

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy

import static ch.qos.logback.classic.Level.DEBUG

import ch.qos.logback.core.status.OnConsoleStatusListener

scan()
statusListener(OnConsoleStatusListener)
def LOG_PATH = "logs"

appender("STDOUT", ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = "%d{HH:mm:ss.SSS} [%thread] %-1level %logger{36} - %msg%n"
	}
}

appender("ROLLING", RollingFileAppender) {
	file = "${LOG_PATH}/BTicino.log"

	rollingPolicy(SizeAndTimeBasedRollingPolicy) {
    	fileNamePattern = "BTicino-%d{yyyy-MM-dd}.%i.log"
    	maxHistory = 30
    	totalSizeCap = "500MB"
    	maxFileSize = "10MB"

		timeBasedFileNamingAndTriggeringPolicy("SizeAndTimeBasedFNATP") {
			maxFileSize = "10MB"
		}
	}

	encoder(PatternLayoutEncoder) {
		pattern = "%-4relative [%thread] %-5level %logger - %msg%n"
	}
}

// Different thread to write because if need to make rooling need time
appender("Async-Appender", AsyncAppender) {
    appenderRef("ROLLING")
}

logger("jndi", INFO)
logger("co.gi9.ilsaul.own", INFO)
logger("org.programmatori.domotica.own", INFO)
root(DEBUG, ["STDOUT", "Async-Appender"])