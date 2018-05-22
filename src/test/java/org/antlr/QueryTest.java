package org.antlr;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlrfun.JNoSQLQueryExecption;
import org.antlrfun.QueryErrorListener;
import org.antlrfun.SelectLexer;
import org.antlrfun.SelectParser;
import org.antlrfun.SelectWalker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.BitSet;
import java.util.logging.Logger;

public class QueryTest {

    private static Logger LOGGER = Logger.getLogger(QueryTest.class.getName());

    @ParameterizedTest
    @ArgumentsSource(QueryArgumentProvider.class)
    public void shouldExecuteQuery(String query) {
        LOGGER.info("Query: " + query);
        testQuery(query);
    }

    @ParameterizedTest
    @ArgumentsSource(WrongQueryArgumentProvider.class)
    public void shouldNotExecute(String query) {
        LOGGER.info("Query: " + query);
        Assertions.assertThrows(JNoSQLQueryExecption.class, () -> {
            testQuery(query);
        });
    }

    private void testQuery(String query) {
        CharStream stream = CharStreams.fromString(query);
        SelectLexer lexer = new SelectLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SelectParser parser = new SelectParser(tokens);
        lexer.addErrorListener(QueryErrorListener.INSTANCE);
        parser.addErrorListener(QueryErrorListener.INSTANCE);

        ParseTree tree = parser.query();
        ParseTreeWalker walker = new ParseTreeWalker();


        walker.walk(new SelectWalker(), tree);


    }


}
