/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.constraintlayout.coremp

import androidx.constraintlayout.coremp.test.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinearSystemTest {
    var mLS: LinearSystem? = null

    @BeforeTest
    fun setUp() {
        mLS = LinearSystem()
        LinearEquation.resetNaming()
    }

    fun add(equation: LinearEquation?) {
        val row1 = LinearEquation.createRowFromEquation(mLS!!, equation!!)
        mLS!!.addConstraint(row1)
    }

    fun add(equation: LinearEquation, strength: Int) {
        println("Add equation <$equation>")
        val row1 = LinearEquation.createRowFromEquation(mLS!!, equation)
        println("Add equation row <$row1>")
        row1.addError(mLS!!, strength)
        mLS!!.addConstraint(row1)
    }

    @Test
    fun testMinMax() {
        // this shows how basic min/max + wrap works.
        // Need to modify ConstraintWidget to generate this.
//        solver.addConstraint(new ClLinearEquation(Rl, 0));
//        solver.addConstraint(new ClLinearEquation(Al, 0));
//        solver.addConstraint(new ClLinearEquation(Bl, 0));
//        solver.addConstraint(new ClLinearEquation(Br, Plus(Bl, 1000)));
//        solver.addConstraint(new ClLinearEquation(Al,
//              new ClLinearExpression(Rl), ClStrength.weak));
//        solver.addConstraint(new ClLinearEquation(Ar,
//              new ClLinearExpression(Rr), ClStrength.weak));
//        solver.addConstraint(new ClLinearInequality(Ar, GEQ, Plus(Al, 150), ClStrength.medium));
//        solver.addConstraint(new ClLinearInequality(Ar, LEQ, Plus(Al, 200), ClStrength.medium));
//        solver.addConstraint(new ClLinearInequality(Rr, GEQ, new ClLinearExpression(Br)));
//        solver.addConstraint(new ClLinearInequality(Rr, GEQ, new ClLinearExpression(Ar)));
        add(LinearEquation(mLS).`var`("Rl").equalsTo().`var`(0))
        //        add(new LinearEquation(s).var("Al").equalsTo().var(0));
//        add(new LinearEquation(s).var("Bl").equalsTo().var(0));
        add(LinearEquation(mLS).`var`("Br").equalsTo().`var`("Bl").plus(300))
        add(LinearEquation(mLS).`var`("Al").equalsTo().`var`("Rl"), 1)
        add(LinearEquation(mLS).`var`("Ar").equalsTo().`var`("Rr"), 1)
        add(LinearEquation(mLS).`var`("Ar").greaterThan().`var`("Al").plus(150), 2)
        add(LinearEquation(mLS).`var`("Ar").lowerThan().`var`("Al").plus(200), 2)
        add(LinearEquation(mLS).`var`("Rr").greaterThan().`var`("Ar"))
        add(LinearEquation(mLS).`var`("Rr").greaterThan().`var`("Br"))
        add(LinearEquation(mLS).`var`("Al").minus("Rl").equalsTo().`var`("Rr").minus("Ar"))
        add(LinearEquation(mLS).`var`("Bl").minus("Rl").equalsTo().`var`("Rr").minus("Br"))
        try {
            mLS!!.minimize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("Result: ")
        mLS!!.displayReadableRows()
        assertEquals(mLS!!.getValueFor("Al").toInt(), 50.0f, 0f)
        assertEquals(mLS!!.getValueFor("Ar").toInt(), 250.0f, 0f)
        assertEquals(mLS!!.getValueFor("Bl").toInt(), 0.0f, 0f)
        assertEquals(mLS!!.getValueFor("Br").toInt(), 300.0f, 0f)
        assertEquals(mLS!!.getValueFor("Rr").toInt(), 300.0f, 0f)
    }

    @Test
    fun testPriorityBasic() {
        add(LinearEquation(mLS).`var`(2, "Xm").equalsTo().`var`("Xl").plus("Xr"))
        add(LinearEquation(mLS).`var`("Xl").plus(10).lowerThan().`var`("Xr"))
        //       add(new LinearEquation(s).var("Xl").greaterThan().var(0));
        add(LinearEquation(mLS).`var`("Xr").lowerThan().`var`(100))
        add(LinearEquation(mLS).`var`("Xm").equalsTo().`var`(50), 2)
        add(LinearEquation(mLS).`var`("Xl").equalsTo().`var`(30), 1)
        add(LinearEquation(mLS).`var`("Xr").equalsTo().`var`(60), 1)
        try {
            mLS!!.minimize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("Result: ")
        mLS!!.displayReadableRows()
        assertEquals(mLS!!.getValueFor("Xm").toInt(), 50.0f, 0f) // 50
        assertEquals(mLS!!.getValueFor("Xl").toInt(), 40.0f, 0f) // 30
        assertEquals(mLS!!.getValueFor("Xr").toInt(), 60.0f, 0f) // 70
    }

    @Test
    fun testPriorities() {
        // | <- a -> | b
        // a - zero = c - a
        // 2a = c + zero
        // a = (c + zero ) / 2
        add(LinearEquation(mLS).`var`("b").equalsTo().`var`(100), 3)
        add(LinearEquation(mLS).`var`("zero").equalsTo().`var`(0), 3)
        add(LinearEquation(mLS).`var`("a").equalsTo().`var`(300), 0)
        add(LinearEquation(mLS).`var`("c").equalsTo().`var`(200), 0)
        add(LinearEquation(mLS).`var`("c").lowerThan().`var`("b").minus(10), 2)
        add(LinearEquation(mLS).`var`("a").lowerThan().`var`("c"), 2)
        add(LinearEquation(mLS).`var`("a").minus("zero").equalsTo().`var`("c").minus("a"), 1)
        try {
            mLS!!.minimize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("Result: ")
        mLS!!.displayReadableRows()
        assertEquals(mLS!!.getValueFor("zero").toInt(), 0.0f, 0f)
        assertEquals(mLS!!.getValueFor("a").toInt(), 45.0f, 0f)
        assertEquals(mLS!!.getValueFor("b").toInt(), 100.0f, 0f)
        assertEquals(mLS!!.getValueFor("c").toInt(), 90.0f, 0f)
    }

    @Test
    fun testOptimizeAndPriority() {
        mLS!!.reset()
        val eq1 = LinearEquation(mLS)
        val eq2 = LinearEquation(mLS)
        val eq3 = LinearEquation(mLS)
        val eq4 = LinearEquation(mLS)
        val eq5 = LinearEquation(mLS)
        val eq6 = LinearEquation(mLS)
        val eq7 = LinearEquation(mLS)
        val eq8 = LinearEquation(mLS)
        val eq9 = LinearEquation(mLS)
        val eq10 = LinearEquation(mLS)
        eq1.`var`("Root.left").equalsTo().`var`(0)
        eq2.`var`("Root.right").equalsTo().`var`(600)
        eq3.`var`("A.right").equalsTo().`var`("A.left").plus(100) // *
        eq4.`var`("A.left").greaterThan().`var`("Root.left") // *
        eq10.`var`("A.left").equalsTo().`var`("Root.left") // *
        eq5.`var`("A.right").lowerThan().`var`("B.left")
        eq6.`var`("B.right").greaterThan().`var`("B.left")
        eq7.`var`("B.right").lowerThan().`var`("Root.right")
        eq8.`var`("B.left").equalsTo().`var`("A.right")
        eq9.`var`("B.right").greaterThan().`var`("Root.right")
        val row1 = LinearEquation.createRowFromEquation(mLS!!, eq1)
        mLS!!.addConstraint(row1)
        val row2 = LinearEquation.createRowFromEquation(mLS!!, eq2)
        mLS!!.addConstraint(row2)
        val row3 = LinearEquation.createRowFromEquation(mLS!!, eq3)
        mLS!!.addConstraint(row3)
        val row10 = LinearEquation.createRowFromEquation(mLS!!, eq10)
        mLS!!.addSingleError(row10, 1, SolverVariable.STRENGTH_MEDIUM)
        mLS!!.addSingleError(row10, -1, SolverVariable.STRENGTH_MEDIUM)
        mLS!!.addConstraint(row10)
        val row4 = LinearEquation.createRowFromEquation(mLS!!, eq4)
        mLS!!.addSingleError(row4, -1, SolverVariable.STRENGTH_HIGH)
        mLS!!.addConstraint(row4)
        val row5 = LinearEquation.createRowFromEquation(mLS!!, eq5)
        mLS!!.addSingleError(row5, 1, SolverVariable.STRENGTH_MEDIUM)
        mLS!!.addConstraint(row5)
        val row6 = LinearEquation.createRowFromEquation(mLS!!, eq6)
        mLS!!.addSingleError(row6, -1, SolverVariable.STRENGTH_LOW)
        mLS!!.addConstraint(row6)
        val row7 = LinearEquation.createRowFromEquation(mLS!!, eq7)
        mLS!!.addSingleError(row7, 1, SolverVariable.STRENGTH_LOW)
        mLS!!.addConstraint(row7)
        val row8 = LinearEquation.createRowFromEquation(mLS!!, eq8)
        row8.addError(mLS!!, SolverVariable.STRENGTH_LOW)
        mLS!!.addConstraint(row8)
        val row9 = LinearEquation.createRowFromEquation(mLS!!, eq9)
        mLS!!.addSingleError(row9, -1, SolverVariable.STRENGTH_LOW)
        mLS!!.addConstraint(row9)
        try {
            mLS!!.minimize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun testPriority() {
        for (i in 0..2) {
            println("\n*** TEST PRIORITY ***\n")
            mLS!!.reset()
            val eq1 = LinearEquation(mLS)
            eq1.`var`("A").equalsTo().`var`(10)
            val row1 = LinearEquation.createRowFromEquation(mLS!!, eq1)
            row1.addError(mLS!!, i % 3)
            mLS!!.addConstraint(row1)
            val eq2 = LinearEquation(mLS)
            eq2.`var`("A").equalsTo().`var`(100)
            val row2 = LinearEquation.createRowFromEquation(mLS!!, eq2)
            row2.addError(mLS!!, (i + 1) % 3)
            mLS!!.addConstraint(row2)
            val eq3 = LinearEquation(mLS)
            eq3.`var`("A").equalsTo().`var`(1000)
            val row3 = LinearEquation.createRowFromEquation(mLS!!, eq3)
            row3.addError(mLS!!, (i + 2) % 3)
            mLS!!.addConstraint(row3)
            try {
                mLS!!.minimize()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            println("Check at iteration $i")
            mLS!!.displayReadableRows()
            when (i) {
                0 -> {
                    assertEquals(
                        mLS!!.getValueFor("A").toInt(),
                        1000.0f,
                        0f,
                    )
                }
                1 -> {
                    assertEquals(
                        mLS!!.getValueFor("A").toInt(),
                        100.0f,
                        0f,
                    )
                }
                2 -> {
                    assertEquals(
                        mLS!!.getValueFor("A").toInt(),
                        10.0f,
                        0f,
                    )
                }
            }
        }
    }

    @Test
    fun testAddEquation1() {
        val e1 = LinearEquation(mLS)
        e1.`var`("W3.left").equalsTo().`var`(0)
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e1))
        // s.rebuildGoalFromErrors();
        val result = mLS!!.getGoal().toString()
        assertTrue(result == "0 = 0.0" || result == " goal -> (0.0) : ")
        assertEquals(
            mLS!!.getValueFor("W3.left").toInt(),
            0.0f,
            0f,
        )
    }

    @Test
    fun testAddEquation2() {
        val e1 = LinearEquation(mLS)
        e1.`var`("W3.left").equalsTo().`var`(0)
        val e2 = LinearEquation(mLS)
        e2.`var`("W3.right").equalsTo().`var`(600)
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e1))
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e2))
        // s.rebuildGoalFromErrors();
        val result = mLS!!.getGoal().toString()
        assertTrue(result == "0 = 0.0" || result == " goal -> (0.0) : ")
        assertEquals(mLS!!.getValueFor("W3.left").toInt(), 0.0f, 0f)
        assertEquals(mLS!!.getValueFor("W3.right").toInt(), 600.0f, 0f)
    }

    @Test
    fun testAddEquation3() {
        val e1 = LinearEquation(mLS)
        e1.`var`("W3.left").equalsTo().`var`(0)
        val e2 = LinearEquation(mLS)
        e2.`var`("W3.right").equalsTo().`var`(600)
        val left_constraint = LinearEquation(mLS)
        left_constraint.`var`("W4.left").equalsTo().`var`("W3.left") // left constraint
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e1))
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e2))
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, left_constraint)) // left
        // s.rebuildGoalFromErrors();
        assertEquals(mLS!!.getValueFor("W3.left").toInt(), 0.0f, 0f)
        assertEquals(mLS!!.getValueFor("W3.right").toInt(), 600.0f, 0f)
        assertEquals(mLS!!.getValueFor("W4.left").toInt(), 0.0f, 0f)
    }

    @Test
    fun testAddEquation4() {
        val e1 = LinearEquation(mLS)
        val e2 = LinearEquation(mLS)
        val e3 = LinearEquation(mLS)
        val e4 = LinearEquation(mLS)
        e1.`var`(2, "Xm").equalsTo().`var`("Xl").plus("Xr")
        val goalRow = mLS!!.getGoal()
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e1)) // 2 Xm = Xl + Xr
        goalRow!!.addError(mLS!!.getVariable("Xm", SolverVariable.Type.ERROR))
        goalRow.addError(mLS!!.getVariable("Xl", SolverVariable.Type.ERROR))
        //        assertEquals(s.getRow(0).toReadableString(), "Xm = 0.5 Xl + 0.5 Xr", 0f);
        e2.`var`("Xl").plus(10).lowerThan().`var`("Xr")
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e2)) // Xl + 10 <= Xr

//        assertEquals(s.getRow(0).toReadableString(), "Xm = 5.0 + Xl + 0.5 s1", 0f);
//        assertEquals(s.getRow(1).toReadableString(), "Xr = 10.0 + Xl + s1", 0f);
        e3.`var`("Xl").greaterThan().`var`(-10)
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e3)) // Xl >= -10
        //        assertEquals(s.getRow(0).toReadableString(), "Xm = -5.0 + 0.5 s1 + s2", 0f);
//        assertEquals(s.getRow(1).toReadableString(), "Xr = s1 + s2", 0f);
//        assertEquals(s.getRow(2).toReadableString(), "Xl = -10.0 + s2", 0f);
        e4.`var`("Xr").lowerThan().`var`(100)
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e4)) // Xr <= 100
        //        assertEquals(s.getRow(0).toReadableString(), "Xm = 45.0 + 0.5 s2 - 0.5 s3", 0f);
//        assertEquals(s.getRow(1).toReadableString(), "Xr = 100.0 - s3", 0f);
//        assertEquals(s.getRow(2).toReadableString(), "Xl = -10.0 + s2", 0f);
//        assertEquals(s.getRow(3).toReadableString(), "s1 = 100.0 - s2 - s3", 0f);
        // s.rebuildGoalFromErrors();
//        assertEquals(s.getGoal().toString(), "Goal: ", 0f);
        val goal = LinearEquation(mLS)
        goal.`var`("Xm").minus("Xl")
        try {
            mLS!!.minimizeGoal(LinearEquation.createRowFromEquation(mLS!!, goal)) // s.getGoal());
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var xl = mLS!!.getValueFor("Xl").toInt()
        var xm = mLS!!.getValueFor("Xm").toInt()
        var xr = mLS!!.getValueFor("Xr").toInt()
        //        assertEquals(xl, -10, 0f);
//        assertEquals(xm, 45, 0f);
//        assertEquals(xr, 100, 0f);
        val e5 = LinearEquation(mLS)
        e5.`var`("Xm").equalsTo().`var`(50)
        mLS!!.addConstraint(LinearEquation.createRowFromEquation(mLS!!, e5))
        try {
//            s.minimizeGoal(s.getGoal());
//            s.minimizeGoal(LinearEquation.createRowFromEquation(s, goal)); //s.getGoal());
            mLS!!.minimizeGoal(goalRow)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        xl = mLS!!.getValueFor("Xl").toInt()
        xm = mLS!!.getValueFor("Xm").toInt()
        xr = mLS!!.getValueFor("Xr").toInt()
        assertEquals(xl, 0)
        assertEquals(xm, 50)
        assertEquals(xr, 100)
    }
}
