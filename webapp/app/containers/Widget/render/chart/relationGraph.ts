/*
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

import { IChartProps } from '../../components/Chart'

export default function (chartProps: IChartProps, drillOptions?: any) {
    // 维度数一定为2，指标数大于等于1
    const { cols, data, chartStyles, color } = chartProps
    const { spec } = chartStyles

    // 顶层节点数量，默认为5，支持配置
    // 点击某个节点后，顶层节点变为1个
    let rootNodeCount = spec.rootNodeCount
    // 如果指定了1个顶层节点时，要用这个值
    let rootNodeName = spec.rootNodeName
    // 度数，默认为3，支持配置
    let linksLevel = spec.linksLevel
    // 节点大小，支持配置
    let symbolSize = spec.symbolSize
    // 节点上的字体大小
    let nodeFontSize = spec.nodeFontSize
    // 连线上的字体大小
    let linkFontSize = spec.linkFontSize
    // 顶层节点默认的棕色
    const rootNodeColor = '#CC6633'
    // 第一维度的名字
    const firstColName = cols[0].name
    // 第二维度的名字
    const secondColName = cols[1].name

    // 保存所有涉及到的节点
    let nodes = []
    // 有多少度就执行多少次
    for (let k = 0; k < linksLevel; k++) {
        if (k === 0) {
            // 现在是在找根节点，找rootNodeCount数量的节点
            for (let i = 0; i < data.length; i++) {
                if (rootNodeName && rootNodeCount === 1) {
                    // 指定了一个顶层节点时
                    if (rootNodeName === data[i][firstColName]) {
                        nodes.push(data[i][firstColName])
                        break
                    }
                } else {
                    // 有一个或多个顶层个节点时
                    if (!nodes.includes(data[i][firstColName])) {
                        nodes.push(data[i][firstColName])
                        if (nodes.length >= rootNodeCount) break
                    }
                }
            }
        } else {
            // 现在是找子节点，就是所有之前的节点连接到的节点
            const tempNodes = []
            for (let i = 0; i < data.length; i++) {
                if (nodes.includes(data[i][firstColName]) && !nodes.includes(data[i][secondColName])) {
                    // 如果第一维度的值在nodes数组里，就把对应的第二维度的值存进nodes数组中
                    tempNodes.push(data[i][secondColName])
                }
            }
            nodes = Array.from(new Set(nodes.concat(tempNodes)))
        }
    }

    if (nodes.length === 0 && rootNodeCount === 1 && rootNodeName) {
        // 说明此时点击了一个节点，并且这个节点本身只是二级维度，这种情况下，页面上就只显示这一个节点
        nodes.push(rootNodeName)
    }

    let colorValues = {}
    if (Array.isArray(color.items) && color.items[0] && color.items[0].config && color.items[0].config.values) colorValues = color.items[0].config.values

    // 用于作为echarts数据的data
    const tempData = []
    for (let i = 0; i < nodes.length; i++) {
        if (i < rootNodeCount) {
            // 顶层节点，颜色默认为棕色
            tempData.push({
                name: nodes[i] !== null && nodes[i] !== undefined ? nodes[i].toString() : nodes[i],
                itemStyle: {
                    color: rootNodeColor
                },
                label: {
                    fontSize: nodeFontSize
                }
            })
        } else {
            const obj = {
                name: nodes[i] !== null && nodes[i] !== undefined ? nodes[i].toString() : nodes[i],
                itemStyle: {},
                label: {
                    fontSize: nodeFontSize
                }
            }
            if (colorValues[obj.name]) {
                obj.itemStyle = {
                    color: colorValues[obj.name]
                }
            }
            tempData.push(obj)
        }
    }

    // 获取所有的连接
    const tempLinks = []
    // 度数不够，未展示的数据项的index数组，用于后面提示
    const unShowIndexes = []
    // 存一个 当前所有连线中，第一个指标最大的值
    let maxFirstIndicator = 0
    for (let i = 0; i < data.length; i++) {
        if (nodes.includes(data[i][firstColName]) && nodes.includes(data[i][secondColName])) {
            let formatter = ''
            let firstIndicator = 0
            Object.keys(data[i]).forEach((key, index) => {
                if (key !== firstColName && key !== secondColName) {
                    if (formatter === '') {
                        formatter = key + '：' + data[i][key]
                    } else {
                        formatter += '\n' + key + '：' + data[i][key]
                    }
                    if (index === 2) {
                        // 用第一个指标的值来设置连接线的大小
                        firstIndicator = data[i][key]
                        if (firstIndicator > maxFirstIndicator) maxFirstIndicator = firstIndicator
                    }
                }
            })
            const link = {
                source: data[i][firstColName] !== null && data[i][firstColName] !== undefined ? data[i][firstColName].toString() : data[i][firstColName],
                target: data[i][secondColName] !== null && data[i][secondColName] !== undefined ? data[i][secondColName].toString() : data[i][secondColName],
                label: {
                    show: true,
                    formatter,
                    fontSize: linkFontSize,
                    tooltip: {
                        formatter: '来往资金总和：20\n来往资金次数：20'
                    },
                },
                lineStyle: {
                    // 默认为1，后面根据第一个指标值的大小
                    width: 1,
                    // 用来后续根据第一个指标值的大小设置粗细
                    data: firstIndicator,
                },
            }
            tempLinks.push(link)
        } else if (nodes.includes(data[i][firstColName]) && !nodes.includes(data[i][secondColName])) {
            unShowIndexes.push(i)
        }
    }

    // 给每条线根据第一个指标的值设置宽度(总共10个等级，根据数据进行计算，比如数据是2、5、20，则对应的宽度就依次是1、3、10)
    for (let i = 0; i < tempLinks.length; i++) {
        // 数据小于等于0的，就用最小的默认width：1
        if (tempLinks[i].lineStyle.data > 0) tempLinks[i].lineStyle.width = Math.ceil(tempLinks[i].lineStyle.data / maxFirstIndicator * 10)
    }

    // for (let i = 0; i < unShowIndexes.length; i++) {
    //     tempData.forEach((item, index) => {
    //         if (item.name === data[i][firstColName]) {
    //             // 度数不够，未展示的数据项的index数组，用于后面提示
    //         }
    //     })
    // }

    return {
        tooltip: {
            trigger: 'item',
            formatter:function(params){
                //params是echarts封装的参数
                if (params.dataType === 'node') return params.name
                if (params.dataType === 'edge') return params.data.label.formatter
            }
        },
        series: [
            {
                type: 'graph',
                animation: false,
                layout: 'force',
                force: {
                    repulsion: 500,
                    edgeLength: 500
                },
                symbolSize,
                label: {
                    show: true
                },
                // 是否开启鼠标缩放和平移漫游。默认不开启。如果只想要开启缩放或者平移，可以设置成 'scale' 或者 'move'。设置成 true 为都开启
                roam: true,
                edgeSymbol: ['circle', 'arrow'],
                edgeSymbolSize: [0, 20],
                edgeLabel: {
                    fontSize: 14
                },
                // data: [{
                //     name: 'A',
                //     itemStyle: {
                //         color: rootNodeColor
                //     }
                // }, {
                //     name: 'B'
                // }, {
                //     name: 'C'
                // }, {
                //     name: 'D'
                // }, {
                //     name: 'E'
                // }],
                data: tempData,
                links: tempLinks,
                // links: [{
                //     source: 'A',
                //     target: 'B',
                //     label: {
                //         show: true,
                //         formatter: '来往资金总和：100000\n来往资金次数：2'
                //     },
                //     lineStyle: {
                //         width: 5
                //     }
                // }, {
                //     source: 'B',
                //     target: 'C',
                //     label: {
                //         show: true,
                //         formatter: '来往资金总和：20\n来往资金次数：20'
                //     },
                //     lineStyle: {
                //         width:1,
                //     },
                // }, {
                //     source: 'A',
                //     target: 'D',
                //     label: {
                //         show: true,
                //         formatter: '来往资金总和：100\n来往资金次数：1'
                //     },
                //     lineStyle: {
                //         width: 2
                //     }
                // }, {
                //     source: 'E',
                //     target: 'A',
                //     label: {
                //         show: true,
                //         formatter: '来往资金总和：20000\n来往资金次数：1000'
                //     },
                //     lineStyle: {
                //         width: 4
                //     }
                // }],
            }
        ]
    }
}
