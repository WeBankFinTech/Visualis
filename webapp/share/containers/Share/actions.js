/*-
 * <<
 * Davinci
 * ==
 * Copyright (C) 2016 - 2017 EDP
 * ==
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
 * >>
 */

import {
  LOAD_SHARE_DASHBOARD,
  LOAD_SHARE_DASHBOARD_SUCCESS,
  LOAD_SHARE_WIDGET,
  LOAD_SHARE_WIDGET_SUCCESS,
  LOAD_SHARE_RESULTSET,
  LOAD_SHARE_RESULTSET_SUCCESS,
  SET_INDIVIDUAL_DASHBOARD
} from './constants'

import { promiseActionCreator } from '../../../app/utils/reduxPromisation'

export const getDashboard = promiseActionCreator(LOAD_SHARE_DASHBOARD, ['token'])

export const getWidget = promiseActionCreator(LOAD_SHARE_WIDGET, ['token'])

export const getResultset = promiseActionCreator(LOAD_SHARE_RESULTSET, ['token', 'sql', 'sortby', 'offset', 'limit'])

export function dashboardGetted (dashboard) {
  return {
    type: LOAD_SHARE_DASHBOARD_SUCCESS,
    payload: {
      dashboard
    }
  }
}

export function widgetGetted (widget) {
  return {
    type: LOAD_SHARE_WIDGET_SUCCESS,
    payload: {
      widget
    }
  }
}

export function resultsetGetted (resultset) {
  return {
    type: LOAD_SHARE_RESULTSET_SUCCESS,
    payload: {
      resultset
    }
  }
}

export function setIndividualDashboard (widgetId, token) {
  return {
    type: SET_INDIVIDUAL_DASHBOARD,
    payload: {
      widgetId,
      token
    }
  }
}
