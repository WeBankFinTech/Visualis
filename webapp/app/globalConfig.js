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

export const env = 'production'

export default {
  dev: {
    host: 'http://localhost:3111',
    shareHost: 'http://localhost:3111'
  },
  production: {
    host: '',
    shareHost: ''
  },
  echarts: {
    theme: {
      default: {
        color: ['#F44336', '#673AB7', '#03A9F4', '#4CAF50', '#FFC107', '#FF5722', '#607D8B', '#E91E63', '#3F51B5', '#00BCD4', '#8BC34A', '#FFEB3B', '#795548', '#000000', '#9C27B0', '#2196F3', '#009688', '#CDDC39', '#FF9800', '#9E9E9E'],
        backgroundColor: '#fff',
        graph: {
          color: ['#F44336', '#673AB7', '#03A9F4', '#4CAF50', '#FFC107', '#FF5722', '#607D8B', '#E91E63', '#3F51B5', '#00BCD4', '#8BC34A', '#FFEB3B', '#795548', '#000000', '#9C27B0', '#2196F3', '#009688', '#CDDC39', '#FF9800', '#9E9E9E']
        }
      }
    }
  }
}
