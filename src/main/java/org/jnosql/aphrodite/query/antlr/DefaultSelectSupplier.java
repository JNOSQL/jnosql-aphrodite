/*
 *  Copyright (c) 2018 Otávio Santana and others
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */

package org.jnosql.aphrodite.query.antlr;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jnosql.aphrodite.query.Condition;
import org.jnosql.aphrodite.query.SelectQuery;
import org.jnosql.aphrodite.query.SelectSupplier;
import org.jnosql.aphrodite.query.Sort;
import org.jnosql.aphrodite.query.Value;
import org.jnosql.aphrodite.query.Where;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jnosql.aphrodite.query.Operator.BETWEEN;
import static org.jnosql.aphrodite.query.Operator.EQUALS;
import static org.jnosql.aphrodite.query.Operator.GREATER_EQUALS_THAN;
import static org.jnosql.aphrodite.query.Operator.GREATER_THAN;
import static org.jnosql.aphrodite.query.Operator.LESSER_EQUALS_THAN;
import static org.jnosql.aphrodite.query.Operator.LESSER_THAN;

public class DefaultSelectSupplier extends SelectBaseListener implements SelectSupplier {

    private String entity;

    private List<String> fields = emptyList();

    private List<Sort> sorts = emptyList();

    private long skip;

    private long limit;

    private Where where;

    private Condition condition;

    @Override
    public void exitFields(SelectParser.FieldsContext ctx) {
        this.fields = ctx.name().stream().map(SelectParser.NameContext::getText).collect(toList());
    }

    @Override
    public void exitSkip(SelectParser.SkipContext ctx) {
        this.skip = Long.valueOf(ctx.INT().getText());
    }

    @Override
    public void exitLimit(SelectParser.LimitContext ctx) {
        this.limit = Long.valueOf(ctx.INT().getText());
    }

    @Override
    public void exitEntity(SelectParser.EntityContext ctx) {
        this.entity = ctx.getText();
    }

    @Override
    public void enterOrder(SelectParser.OrderContext ctx) {
        this.sorts = ctx.orderName().stream().map(DefaultSort::of).collect(Collectors.toList());
    }

    @Override
    public void exitEq(SelectParser.EqContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String name = ctx.name().getText();
        Value<?> value = ValueConverter.get(ctx.value());
        this.condition = new DefaultCondition(name, EQUALS, value);
    }

    @Override
    public void exitLt(SelectParser.LtContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String name = ctx.name().getText();
        Value<?> value = ValueConverter.get(ctx.value());
        this.condition = new DefaultCondition(name, LESSER_THAN, value);
    }

    @Override
    public void exitLte(SelectParser.LteContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String name = ctx.name().getText();
        Value<?> value = ValueConverter.get(ctx.value());
        this.condition = new DefaultCondition(name, LESSER_EQUALS_THAN, value);
    }

    @Override
    public void exitGt(SelectParser.GtContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String name = ctx.name().getText();
        Value<?> value = ValueConverter.get(ctx.value());
        this.condition = new DefaultCondition(name, GREATER_THAN, value);
    }

    @Override
    public void exitGte(SelectParser.GteContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String name = ctx.name().getText();
        Value<?> value = ValueConverter.get(ctx.value());
        this.condition = new DefaultCondition(name, GREATER_EQUALS_THAN, value);
    }


    @Override
    public void exitBetween(SelectParser.BetweenContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String name = ctx.name().getText();
        Value[] values = ctx.value().stream().map(ValueConverter::get).toArray(Value[]::new);
        this.condition = new DefaultCondition(name, BETWEEN, DefaultArrayValue.of(values));
    }


    @Override
    public SelectQuery apply(String query) {
        Objects.requireNonNull(query, "query is required");

        CharStream stream = CharStreams.fromString(query);
        SelectLexer lexer = new SelectLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SelectParser parser = new SelectParser(tokens);
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(QueryErrorListener.INSTANCE);
        parser.addErrorListener(QueryErrorListener.INSTANCE);

        ParseTree tree = parser.query();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, tree);
        if(Objects.nonNull(condition)) {
            this.where = new DefaultWhere(condition);
        }
        return new DefaultSelectQuery(entity, fields, sorts, skip, limit, where);
    }
}