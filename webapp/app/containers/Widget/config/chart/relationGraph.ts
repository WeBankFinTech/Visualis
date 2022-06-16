import ChartTypes from './ChartTypes'

import { IChartInfo } from 'containers/Widget/components/Widget'

const relationGraph: IChartInfo = {
  id: ChartTypes.RelationGraph,
  name: 'relationGraph',
  title: '关系图',
  icon: 'icon-relation-graph',
  coordinate: 'cartesian',
  rules: [{ dimension: 2, metric: [1, 9999] }],
  dimetionAxis: 'col',
  data: {
    cols: {
      title: '列',
      type: 'category'
    },
    rows: {
      title: '行',
      type: 'category'
    },
    metrics: {
      title: '指标',
      type: 'value'
    },
    filters: {
      title: '筛选',
      type: 'all'
    },
    color: {
      title: '颜色',
      type: 'category'
    },
  },
  style: {
    // 有这个spec，在“样式”中才有基础的部分 在ConfigSections/SpecSection/specs里的内容
    spec: {
      // 顶层节点数，默认为5，支持配置
      rootNodeCount: 5,
      // 度数，默认为3，支持配置
      linksLevel: 3,
      // 节点大小，默认为40，支持配置
      symbolSize: 40,
      // 如果指定了1个顶层节点时，要用这个值
      rootNodeName: '',
      // 节点上的字体大小
      nodeFontSize: 12,
      // 连线上的字体大小
      linkFontSize: 14
    }
  }
}

export default relationGraph
