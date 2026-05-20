#!/usr/bin/env node

import fs from 'node:fs/promises'
import path from 'node:path'
import { execFileSync } from 'node:child_process'

const BASE_URL = process.env.JAS_BASE_URL || 'http://127.0.0.1:7777/api'
const USER_ID = process.env.JAS_USER_ID || '3384650106421960'
const MERCHANT_ID = process.env.JAS_MERCHANT_ID || '7638432224340229'
const MYSQL_ARGS = ['--default-character-set=utf8mb4', '-uroot', '-p123456', '-D', 'jia_shi_yi_xuan', '-N', '-B']

function createDishImage(label, color) {
  return `https://dummyimage.com/400x300/${color}/ffffff.png&text=${label}`
}

const DISH_SEEDS = [
  {
    name: '南瓜小米粥',
    category: '早餐',
    price: 14,
    calorie: 160,
    description: '暖胃低负担，适合早餐时段推荐',
    image: createDishImage('Pumpkin+Millet', 'f6a623')
  },
  {
    name: '全麦鸡肉卷',
    category: '早餐',
    price: 18,
    calorie: 280,
    description: '高饱腹感早餐主食，适合通勤场景',
    image: createDishImage('Chicken+Wrap', '8bc34a')
  },
  {
    name: '鲜虾蔬菜蒸蛋',
    category: '早餐',
    price: 16,
    calorie: 220,
    description: '蛋白质友好，口味清爽',
    image: createDishImage('Shrimp+Egg', '4fc3f7')
  },
  {
    name: '蓝莓酸奶燕麦杯',
    category: '早餐',
    price: 20,
    calorie: 240,
    description: '轻食型早餐，适合控卡需求',
    image: createDishImage('Berry+Oats', '7e57c2')
  },
  {
    name: '牛油果鸡蛋三明治',
    category: '早餐',
    price: 22,
    calorie: 320,
    description: '营养均衡，适合早午餐推荐',
    image: createDishImage('Avocado+Sandwich', '66bb6a')
  },
  {
    name: '玉米鸡丝粥',
    category: '早餐',
    price: 15,
    calorie: 190,
    description: '清淡暖胃，适合工作日早餐补充能量',
    image: createDishImage('Corn+Chicken+Porridge', 'f4c542')
  },
  {
    name: '紫薯坚果酸奶碗',
    category: '早餐',
    price: 21,
    calorie: 230,
    description: '轻负担高饱腹，适合控卡和早午餐场景',
    image: createDishImage('Purple+Yogurt+Bowl', 'b388eb')
  },
  {
    name: '芝士番茄鸡蛋贝果',
    category: '早餐',
    price: 24,
    calorie: 340,
    description: '高满足感早餐主食，适合早高峰补能',
    image: createDishImage('Cheese+Egg+Bagel', 'ff8a65')
  }
]

if (typeof fetch !== 'function') {
  throw new Error('当前 Node 版本不支持 fetch')
}

function formatDate(date) {
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}_${pad(date.getHours())}${pad(date.getMinutes())}${pad(date.getSeconds())}`
}

function escapeSql(value) {
  return String(value).replace(/\\/g, '\\\\').replace(/'/g, "\\'")
}

function mysql(query) {
  return execFileSync('mysql', [...MYSQL_ARGS, '-e', query], {
    encoding: 'utf8'
  }).trim()
}

function mysqlJson(query, fallback) {
  const output = mysql(query)
  if (!output) return fallback

  const lines = output.split('\n').filter(Boolean)
  const payload = lines[lines.length - 1]
  return payload ? JSON.parse(payload) : fallback
}

async function api(method, endpoint, body) {
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined
  })

  const text = await response.text()
  let data

  try {
    data = text ? JSON.parse(text) : null
  } catch {
    data = { raw: text }
  }

  return {
    ok: response.ok,
    status: response.status,
    data
  }
}

async function ensureSeedDishes() {
  const existing = mysqlJson(
    `
      SELECT COALESCE(
        JSON_ARRAYAGG(
          JSON_OBJECT(
            'id', id,
            'name', name,
            'category', category,
            'price', price,
            'calorie', calorie,
            'image', image
          )
        ),
        JSON_ARRAY()
      )
      FROM t_dish
      WHERE merchant_id = '${escapeSql(MERCHANT_ID)}'
        AND name IN (${DISH_SEEDS.map((dish) => `'${escapeSql(dish.name)}'`).join(',')})
    `,
    []
  )

  const existingByName = new Map(existing.map((dish) => [dish.name, dish]))
  const created = []
  const reused = []

  for (const dish of DISH_SEEDS) {
    if (existingByName.has(dish.name)) {
      const existingDish = existingByName.get(dish.name)
      if (existingDish.image !== dish.image) {
        mysql(`
          UPDATE t_dish
          SET image = '${escapeSql(dish.image)}'
          WHERE id = '${escapeSql(existingDish.id)}'
        `)
      }

      reused.push({
        ...existingDish,
        imageUpdated: existingDish.image !== dish.image
      })
      continue
    }

    const payload = {
      merchantId: MERCHANT_ID,
      name: dish.name,
      category: dish.category,
      price: dish.price,
      calorie: dish.calorie,
      description: dish.description,
      image: dish.image,
      isOnline: true,
      stock: 120,
      avgRating: 4.7,
      ingredients: '[]',
      cookingSteps: '[]',
      nutrition: '{}',
      stepTemplate: 'FAST',
      estimatedCookingMinutes: 10,
      auditStatus: 'APPROVED'
    }

    const result = await api('POST', '/v1/dishes', payload)
    if (!result.ok || result.data?.success !== true) {
      const insertedId = `seed_${Date.now()}_${created.length + 1}`
      mysql(`
        INSERT INTO t_dish (
          id, merchant_id, name, category, price, calorie,
          estimated_cooking_minutes, step_template, description, image,
          is_online, audit_status, stock, avg_rating, order_count, favorite_count
        ) VALUES (
          '${escapeSql(insertedId)}',
          '${escapeSql(MERCHANT_ID)}',
          '${escapeSql(dish.name)}',
          '${escapeSql(dish.category)}',
          ${Number(dish.price).toFixed(2)},
          ${Number(dish.calorie)},
          10,
          'FAST',
          '${escapeSql(dish.description)}',
          '${escapeSql(dish.image)}',
          1,
          'APPROVED',
          120,
          4.70,
          0,
          0
        )
      `)

      created.push({
        id: insertedId,
        ...dish,
        createMethod: 'sql_fallback',
        apiError: result.data
      })
      continue
    }

    created.push({
      ...result.data.data,
      createMethod: 'api'
    })
  }

  const finalDishes = mysqlJson(
    `
      SELECT COALESCE(
        JSON_ARRAYAGG(
          JSON_OBJECT(
            'id', id,
            'name', name,
            'category', category,
            'price', price,
            'calorie', calorie,
            'isOnline', is_online
          )
        ),
        JSON_ARRAY()
      )
      FROM t_dish
      WHERE merchant_id = '${escapeSql(MERCHANT_ID)}'
        AND name IN (${DISH_SEEDS.map((dish) => `'${escapeSql(dish.name)}'`).join(',')})
      ORDER BY create_time DESC
    `,
    []
  )

  return { created, reused, finalDishes }
}

function getPriceLevel(price) {
  if (price < 15) return 1
  if (price < 25) return 2
  if (price < 40) return 3
  if (price < 60) return 4
  return 5
}

function getFeatureTemplate(dish) {
  return {
    tags: ['早餐', '轻负担', '推荐造数'],
    flavorProfile: { salty: 0.3, spicy: 0.1, sweet: 0.2 },
    nutritionInfo: {
      calories: dish.calorie,
      protein: dish.calorie <= 220 ? 12 : 18,
      fat: dish.calorie <= 220 ? 6 : 10,
      carbs: dish.calorie <= 220 ? 24 : 32
    },
    ingredients: ['谷物', '鸡蛋', '蔬菜'],
    cookingMethod: '煮',
    suitableScenarios: ['通勤早餐', '工作日'],
    timePeriodTags: ['早餐'],
    seasonTags: ['春季', '夏季', '秋季', '冬季'],
    priceLevel: getPriceLevel(dish.price),
    popularityScore: 0.82
  }
}

function ensureDishFeatures(dishes) {
  for (const dish of dishes) {
    const exists = mysql(`SELECT COUNT(*) FROM dish_features WHERE dish_id = '${escapeSql(dish.id)}'`)
    if (Number(exists) > 0) {
      continue
    }

    const feature = getFeatureTemplate(dish)
    mysql(`
      INSERT INTO dish_features (
        dish_id, category, tags, flavor_profile, nutrition_info, ingredients,
        cooking_method, suitable_scenarios, time_period_tags, season_tags,
        price_level, popularity_score
      ) VALUES (
        '${escapeSql(dish.id)}',
        '${escapeSql(dish.category)}',
        JSON_ARRAY(${feature.tags.map((tag) => `'${escapeSql(tag)}'`).join(', ')}),
        JSON_OBJECT('salty', ${feature.flavorProfile.salty}, 'spicy', ${feature.flavorProfile.spicy}, 'sweet', ${feature.flavorProfile.sweet}),
        JSON_OBJECT('calories', ${feature.nutritionInfo.calories}, 'protein', ${feature.nutritionInfo.protein}, 'fat', ${feature.nutritionInfo.fat}, 'carbs', ${feature.nutritionInfo.carbs}),
        JSON_ARRAY(${feature.ingredients.map((item) => `'${escapeSql(item)}'`).join(', ')}),
        '${escapeSql(feature.cookingMethod)}',
        JSON_ARRAY(${feature.suitableScenarios.map((item) => `'${escapeSql(item)}'`).join(', ')}),
        JSON_ARRAY(${feature.timePeriodTags.map((item) => `'${escapeSql(item)}'`).join(', ')}),
        JSON_ARRAY(${feature.seasonTags.map((item) => `'${escapeSql(item)}'`).join(', ')}),
        ${feature.priceLevel},
        ${feature.popularityScore}
      )
    `)
  }
}

async function main() {
  const startedAt = new Date()
  const runId = `recommend-seed-${formatDate(startedAt)}`
  const reportDir = path.join(process.cwd(), 'output', 'recommendation-seed')
  await fs.mkdir(reportDir, { recursive: true })

  const startedAtSql = startedAt.toISOString().slice(0, 19).replace('T', ' ')
  const report = {
    runId,
    baseUrl: BASE_URL,
    userId: USER_ID,
    merchantId: MERCHANT_ID,
    startedAt: startedAt.toISOString(),
    assumptions: [
      '目标用户使用当前桌面端常见测试账号 17322222222',
      '优先通过后端接口创建菜品和推荐行为',
      '通过 recommendation_log、user_behavior、t_reject_recommendation 的时间窗口确认本次造数结果'
    ]
  }

  report.countsBefore = mysqlJson(
    `
      SELECT JSON_OBJECT(
        'recommendationLog', (SELECT COUNT(*) FROM recommendation_log WHERE user_id = '${escapeSql(USER_ID)}'),
        'userBehavior', (SELECT COUNT(*) FROM user_behavior WHERE user_id = '${escapeSql(USER_ID)}'),
        'rejectRecommendation', (SELECT COUNT(*) FROM t_reject_recommendation WHERE user_id = '${escapeSql(USER_ID)}')
      )
    `,
    {}
  )

  report.seedDishes = await ensureSeedDishes()
  ensureDishFeatures(report.seedDishes.finalDishes)

  const breakfastDishes = mysqlJson(
    `
      SELECT COALESCE(
        JSON_ARRAYAGG(
          JSON_OBJECT(
            'id', id,
            'name', name,
            'category', category
          )
        ),
        JSON_ARRAY()
      )
      FROM t_dish
      WHERE merchant_id = '${escapeSql(MERCHANT_ID)}'
        AND category = '早餐'
        AND is_online = 1
      ORDER BY create_time DESC
    `,
    []
  )

  if (breakfastDishes.length < 4) {
    throw new Error('早餐菜品不足，无法生成足够的推荐数据')
  }
  const breakfastDishIds = new Set(breakfastDishes.map((dish) => dish.id))

  report.bootstrapBehaviors = []
  for (const dish of breakfastDishes.slice(0, 4)) {
    const behaviorResult = await api('POST', '/v1/recommendations/behavior', {
      userId: USER_ID,
      behaviorType: 'view',
      itemType: 'dish',
      itemId: dish.id,
      duration: 18,
      context: {
        source: 'codex_seed',
        runId,
        phase: 'bootstrap_view',
        category: dish.category
      }
    })
    report.bootstrapBehaviors.push({
      dishId: dish.id,
      dishName: dish.name,
      response: behaviorResult.data
    })
  }

  report.profileBoostFeedback = []
  for (const dish of breakfastDishes.slice(0, 3)) {
    const feedbackResult = await api('POST', '/v1/recommendations/feedback', {
      userId: USER_ID,
      dishId: dish.id,
      recommendationId: `${runId}-bootstrap`,
      isClicked: true,
      isOrdered: true
    })
    report.profileBoostFeedback.push({
      dishId: dish.id,
      dishName: dish.name,
      response: feedbackResult.data
    })
  }

  const profileAfterBoost = await api('GET', `/v1/recommendations/profile/${USER_ID}`)
  report.profileAfterBoost = profileAfterBoost.data

  const recommendationResponse = await api(
    'GET',
    `/v1/recommendations/${USER_ID}?scene=home&limit=20&timePeriod=${encodeURIComponent('早餐')}`
  )
  report.recommendationResponse = recommendationResponse.data

  await new Promise((resolve) => setTimeout(resolve, 500))

  const latestBatches = mysqlJson(
    `
      SELECT COALESCE(
        JSON_ARRAYAGG(
          JSON_OBJECT(
            'recommendationId', recommendation_id,
            'createdTime', created_time,
            'itemCount', item_count
          )
        ),
        JSON_ARRAY()
      )
      FROM (
        SELECT recommendation_id, MAX(created_time) AS created_time, COUNT(*) AS item_count
        FROM recommendation_log
        WHERE user_id = '${escapeSql(USER_ID)}'
          AND created_time >= '${startedAtSql}'
        GROUP BY recommendation_id
        ORDER BY created_time DESC
        LIMIT 5
      ) batch_logs
    `,
    []
  )

  report.recommendationBatches = latestBatches
  const latestRecommendationId = latestBatches[0]?.recommendationId

    if (latestRecommendationId) {
    const recommendationItems = mysqlJson(
      `
        SELECT COALESCE(
          JSON_ARRAYAGG(
            JSON_OBJECT(
              'dishId', dish_id,
              'rank', \`rank\`,
              'score', score,
              'reason', reason
            )
          ),
          JSON_ARRAY()
        )
        FROM recommendation_log
        WHERE recommendation_id = '${escapeSql(latestRecommendationId)}'
        ORDER BY \`rank\` ASC
      `,
      []
    )

    report.latestRecommendationItems = recommendationItems

      report.followupFeedback = []
      let breakfastOrderUsed = false
      for (const item of recommendationItems.slice(0, 3)) {
        const linkedDish = breakfastDishes.find((dish) => dish.id === item.dishId)
        const shouldOrder = breakfastDishIds.has(item.dishId) && !breakfastOrderUsed
        const feedbackResult = await api('POST', '/v1/recommendations/feedback', {
          userId: USER_ID,
          dishId: item.dishId,
          recommendationId: latestRecommendationId,
          isClicked: true,
          isOrdered: shouldOrder
        })
        if (shouldOrder) {
          breakfastOrderUsed = true
        }

        report.followupFeedback.push({
          dishId: item.dishId,
          dishName: linkedDish?.name || item.dishId,
          recommendationId: latestRecommendationId,
          ordered: shouldOrder,
          response: feedbackResult.data
        })
      }

      const rejectTarget = recommendationItems.find((item) => !breakfastDishIds.has(item.dishId))
        || recommendationItems[recommendationItems.length - 1]
    if (rejectTarget) {
      const rejectDish = breakfastDishes.find((dish) => dish.id === rejectTarget.dishId)
      const rejectResult = await api('POST', `/v1/recommendations/${USER_ID}/reject`, {
        dishId: rejectTarget.dishId,
        reason: `本次造数拒绝记录：${runId}`
      })
      report.rejectAction = {
        dishId: rejectTarget.dishId,
        dishName: rejectDish?.name || rejectTarget.dishId,
        response: rejectResult.data
      }
    }
  }

  const refreshResult = await api('POST', `/v1/recommendations/${USER_ID}/refresh`)
  report.refreshResult = refreshResult.data

  await new Promise((resolve) => setTimeout(resolve, 1200))

  report.countsAfter = mysqlJson(
    `
      SELECT JSON_OBJECT(
        'recommendationLog', (SELECT COUNT(*) FROM recommendation_log WHERE user_id = '${escapeSql(USER_ID)}'),
        'userBehavior', (SELECT COUNT(*) FROM user_behavior WHERE user_id = '${escapeSql(USER_ID)}'),
        'rejectRecommendation', (SELECT COUNT(*) FROM t_reject_recommendation WHERE user_id = '${escapeSql(USER_ID)}')
      )
    `,
    {}
  )

  report.newRecommendationLogs = mysqlJson(
    `
      SELECT COALESCE(
        JSON_ARRAYAGG(
          JSON_OBJECT(
            'recommendationId', recommendation_id,
            'dishId', dish_id,
            'rank', \`rank\`,
            'score', score,
            'isClicked', is_clicked,
            'isOrdered', is_ordered,
            'createdTime', created_time
          )
        ),
        JSON_ARRAY()
      )
      FROM (
        SELECT recommendation_id, dish_id, \`rank\`, score, is_clicked, is_ordered, created_time
        FROM recommendation_log
        WHERE user_id = '${escapeSql(USER_ID)}'
          AND created_time >= '${startedAtSql}'
        ORDER BY created_time DESC, \`rank\` ASC
      ) recent_logs
    `,
    []
  )

  report.newUserBehaviors = mysqlJson(
    `
      SELECT COALESCE(
        JSON_ARRAYAGG(
          JSON_OBJECT(
            'behaviorType', behavior_type,
            'itemType', item_type,
            'itemId', item_id,
            'context', context,
            'createdTime', created_time
          )
        ),
        JSON_ARRAY()
      )
      FROM (
        SELECT behavior_type, item_type, item_id, context, created_time
        FROM user_behavior
        WHERE user_id = '${escapeSql(USER_ID)}'
          AND created_time >= '${startedAtSql}'
        ORDER BY created_time DESC
      ) recent_behaviors
    `,
    []
  )

  report.newRejectRecords = mysqlJson(
    `
      SELECT COALESCE(
        JSON_ARRAYAGG(
          JSON_OBJECT(
            'dishId', dish_id,
            'reason', reason,
            'rejectTime', reject_time
          )
        ),
        JSON_ARRAY()
      )
      FROM (
        SELECT dish_id, reason, reject_time
        FROM t_reject_recommendation
        WHERE user_id = '${escapeSql(USER_ID)}'
          AND reject_time >= '${startedAtSql}'
        ORDER BY reject_time DESC
      ) reject_logs
    `,
    []
  )

  report.finalProfile = (await api('GET', `/v1/recommendations/profile/${USER_ID}`)).data
  report.finishedAt = new Date().toISOString()

  const jsonPath = path.join(reportDir, `${runId}.json`)
  const mdPath = path.join(reportDir, `${runId}.md`)

  await fs.writeFile(jsonPath, `${JSON.stringify(report, null, 2)}\n`, 'utf8')

  const markdown = [
    `# 推荐造数报告`,
    ``,
    `- 运行ID：\`${runId}\``,
    `- 用户ID：\`${USER_ID}\``,
    `- 商家ID：\`${MERCHANT_ID}\``,
    `- 开始时间：\`${report.startedAt}\``,
    `- 结束时间：\`${report.finishedAt}\``,
    ``,
    `## 概览`,
    `- 新建菜品数：${report.seedDishes.created.length}`,
    `- 复用已有菜品数：${report.seedDishes.reused.length}`,
    `- 推荐日志增量：${report.countsAfter.recommendationLog - report.countsBefore.recommendationLog}`,
    `- 用户行为增量：${report.countsAfter.userBehavior - report.countsBefore.userBehavior}`,
    `- 拒绝记录增量：${report.countsAfter.rejectRecommendation - report.countsBefore.rejectRecommendation}`,
    ``,
    `## 新建或复用的早餐菜品`,
    ...report.seedDishes.finalDishes.map((dish) => `- \`${dish.id}\` ${dish.name} / ${dish.category} / ${dish.calorie} kcal / ¥${dish.price}`),
    ``,
    `## 推荐批次`,
    ...(report.recommendationBatches.length
      ? report.recommendationBatches.map((batch) => `- \`${batch.recommendationId}\` ${batch.createdTime} / ${batch.itemCount} 条`)
      : ['- 本次未获取到推荐批次']),
    ``,
    `## 报告文件`,
    `- JSON：\`${jsonPath}\``,
    `- Markdown：\`${mdPath}\``
  ].join('\n')

  await fs.writeFile(mdPath, `${markdown}\n`, 'utf8')

  console.log(JSON.stringify({
    runId,
    jsonPath,
    mdPath,
    countsBefore: report.countsBefore,
    countsAfter: report.countsAfter
  }, null, 2))
}

main().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
