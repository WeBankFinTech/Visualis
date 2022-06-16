export interface ISpecConfig {
  smooth?: boolean
  stack?: boolean
  barChart?: boolean
  percentage?: boolean
  step?: boolean
  roseType?: boolean
  circle?: boolean
  sortMode?: string
  alignmentMode?: string
  gapNumber?: number
  shape?: 'polygon' | 'circle'
  roam?: boolean
  layerType?: string
  linesSpeed: number
  symbolType: string
  layout?: 'horizontal' | 'vertical'

  // for sankey
  nodeWidth: number
  nodeGap: number
  orient: 'horizontal' | 'vertical'
  draggable: boolean
  symbol?: boolean
  label?: boolean

  // for relationGraph
  // 顶层节点数，默认为5，支持配置
  rootNodeCount: number
  // 度数，默认为3，支持配置
  linksLevel: number
  // 节点大小，默认为120，支持配置
  symbolSize: number
  // 如果指定了1个顶层节点时，要用这个值
  rootNodeName: string
  // 节点上的字体大小
  nodeFontSize: number
  // 连线上的字体大小
  linkFontSize: number
}
