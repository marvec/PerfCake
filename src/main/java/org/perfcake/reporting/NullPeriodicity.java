/*
 * Copyright 2010-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.perfcake.reporting;

/**
 * This object represents periodicity that is null (no periodical outputs to the
 * destinations.
 */
public class NullPeriodicity extends Periodicity {
   public NullPeriodicity() {
   }

   public NullPeriodicity(float value, PeriodicityUnit unit) {
   }

   @Override
   public void setTestRunInfo(TestRunInfo tri) {
   }

   @Override
   public boolean isTimely() {
      return false;
   }

   @Override
   public boolean isIterationary() {
      return false;
   }

   @Override
   public int getIterationalPeriodicity() {
      throw new ReportsException("Null periodicity doesn't have iterational periodicity");
   }

   @Override
   public float getTimePeriodicity() {
      throw new ReportsException("Null periodicity doesn't have time periodicity");
   }

   @Override
   public String toString() {
      return "{Null periodicity}";
   }
}
