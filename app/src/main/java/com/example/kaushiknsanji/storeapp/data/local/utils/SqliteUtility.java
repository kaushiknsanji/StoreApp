/*
 * Copyright 2018 Kaushik N. Sanji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kaushiknsanji.storeapp.data.local.utils;

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class that provides constants for use with Query construction and Schema Construction.
 * Also, provides methods for the construction of complex selection clause.
 *
 * @author Kaushik N Sanji
 */
public final class SqliteUtility {

    //Query construction related constants
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String NOT = " NOT ";
    public static final String JOIN = " JOIN ";
    public static final String ON = " ON ";
    public static final String LEFT_JOIN = " LEFT JOIN ";
    public static final String EQUALS = " = ";
    public static final String IS = " IS ";
    public static final String DESC = " DESC ";
    public static final String PLACEHOLDER = "?";
    public static final String NULL = "NULL";
    public static final String COUNT = "COUNT";
    public static final String SUM = "SUM";
    public static final String AS = " AS ";
    public static final String OPEN_BRACE = "(";
    public static final String CLOSE_BRACE = ")";
    public static final String SPACE = " ";
    public static final String COMMA = ",";

    //Schema construction related constants
    public static final String CREATE_TABLE = "CREATE TABLE ";
    public static final String CREATE_INDEX = "CREATE INDEX ";
    public static final String INTEGER = "INTEGER";
    public static final String TEXT = "TEXT";
    public static final String REAL = "REAL";
    public static final String PRIMARY_KEY = "PRIMARY KEY";
    public static final String PRIMARY_KEY_AUTOINCREMENT = PRIMARY_KEY + SPACE + "AUTOINCREMENT";
    public static final String CONFLICT_FAIL = "CONFLICT FAIL";
    public static final String CONFLICT_REPLACE = "CONFLICT REPLACE";
    public static final String DEFAULT = " DEFAULT ";
    public static final String FOREIGN_KEY = " FOREIGN KEY ";
    public static final String REFERENCES = " REFERENCES ";
    public static final String CONSTRAINT = " CONSTRAINT ";
    public static final String UNIQUE = " UNIQUE ";
    public static final String DELETE_CASCADE = "DELETE CASCADE";

    /**
     * Private Constructor to prevent instantiation of {@link SqliteUtility}
     */
    private SqliteUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    /**
     * Method that prepares the Selection and Selection Arguments of the WHERE clause
     * for the IN Clause query. IN Clause query is prepared by adjoining "OR" for every value of {@code possibleColumnValues}
     * instead of framing the regular IN Clause.
     *
     * @param columnName           The Name of the Column involved in the IN Clause
     * @param possibleColumnValues List Of String Values possible for the column {@code columnName} involved.
     * @return A Pair of Selection with its Selection Arguments for the parameters passed. Can be {@code null}
     * when the input parameters are {@code null} or empty.
     */
    @Nullable
    public static Pair<String, String[]> makeSelectionForInClause(String columnName,
                                                                  List<String> possibleColumnValues) {

        if (possibleColumnValues != null && possibleColumnValues.size() > 0 && !TextUtils.isEmpty(columnName)) {
            //When the parameters passed are not null or empty

            //StringBuilder for the Selection Clause
            StringBuilder selectionClauseBuilder = new StringBuilder();
            //StringBuilder for the Selection Clause Arguments
            StringBuilder selectionArgsBuilder = new StringBuilder();

            //Iterating over the possible list of column values to build the Selection Clause and Arguments
            for (String columnValue : possibleColumnValues) {
                if (selectionClauseBuilder.length() > 0) {
                    //When the selection clause builder is already having content, then
                    //we need to append it with "OR" and its argument builder with ","
                    selectionClauseBuilder.append(OR);
                    selectionArgsBuilder.append(COMMA);
                }
                //Appending "ColumnName = ?"
                selectionClauseBuilder.append(columnName).append(EQUALS).append(PLACEHOLDER);
                //Appending the value of Column for the Placeholder
                selectionArgsBuilder.append(columnValue);
            }

            //Generate the strings built
            String selectionClause = selectionClauseBuilder.toString();
            String selectionArgs = selectionArgsBuilder.toString();

            //Returning the Pair for Selection with Selection Arguments
            return Pair.create(
                    selectionClause,
                    //Splitting with "," to generate an Array of Strings for Selection Arguments
                    selectionArgs.split(COMMA)
            );
        }

        //Returning Null when the parameters passed is either Null or empty
        return null;
    }

    /**
     * Method that combines the Selection clauses {@code selectionPair1} and {@code selectionPair2}
     * and generates a combined Pair for Selection Clause with its Selection arguments.
     *
     * @param selectionPair1 Pair of Selection Clause with its arguments
     * @param selectionPair2 Pair of Selection Clause with its arguments
     * @param combineLogic   Combining logic to use to combine the given selection clauses.
     *                       This can be one of those defined in @{@link SelectionClauseCombineLogicDef}
     * @return A Pair of combined Selection Clause with its arguments. Can be {@code null}
     * when the input parameters are {@code null} or empty.
     */
    @Nullable
    public static Pair<String, String[]> combineSelectionPairs(Pair<String, String[]> selectionPair1,
                                                               Pair<String, String[]> selectionPair2,
                                                               @SelectionClauseCombineLogicDef String combineLogic) {
        if (selectionPair1 != null && selectionPair2 != null) {
            //When the parameters passed are not null or empty

            //Retrieving the data from the Pairs
            String selectionClause1 = selectionPair1.first;
            String[] selectionArg1 = selectionPair1.second;
            String selectionClause2 = selectionPair2.first;
            String[] selectionArg2 = selectionPair2.second;

            if (TextUtils.isEmpty(selectionClause1) && TextUtils.isEmpty(selectionClause2)) {
                //Returning Null when the selection clauses passed are null or empty
                return null;

            } else if (!TextUtils.isEmpty(selectionClause1) && !TextUtils.isEmpty(selectionClause2)) {
                //When both selection clauses are present

                //Generating a combined selection clause using the {@code combineLogic} passed
                //"(selectionClause1) {AND/OR} (selectionClause2)
                String selectionClause = OPEN_BRACE + selectionClause1 + CLOSE_BRACE + combineLogic + OPEN_BRACE + selectionClause2 + CLOSE_BRACE;

                //Initializing a list for combining the Selection Arguments
                ArrayList<String> selectionArgsList = new ArrayList<>();

                if (selectionArg1 != null && selectionArg1.length > 0) {
                    //When there arguments for the first selection clause, add them to the list
                    selectionArgsList.addAll(Arrays.asList(selectionArg1));
                }

                if (selectionArg2 != null && selectionArg2.length > 0) {
                    //When there arguments for the second selection clause, add them to the list
                    selectionArgsList.addAll(Arrays.asList(selectionArg2));
                }

                //Generating the selection arguments array from the selection arguments list
                String[] selectionArgs = selectionArgsList.toArray(new String[0]);

                //Returning the Pair of combined selection clause with its arguments
                return Pair.create(selectionClause, selectionArgs);

            } else {
                //When one of the selection clauses passed is null or empty

                if (!TextUtils.isEmpty(selectionClause1)) {
                    //When we have the first selection clause, return its Pair
                    return selectionPair1;
                } else {
                    //When we have the second selection clause, return its Pair
                    return selectionPair2;
                }
            }
        }

        //Returning Null when the parameters passed is either Null or empty
        return null;
    }

    //Defining the Annotation interface for valid Combine Logic for selection clauses
    //Enumerating the Annotation with valid Combine Logic for selection clauses
    @StringDef({AND, OR})
    //Retaining Annotation till Compile Time
    @Retention(RetentionPolicy.SOURCE)
    @interface SelectionClauseCombineLogicDef {
    }
}
