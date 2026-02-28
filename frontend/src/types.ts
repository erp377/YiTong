export type GuideCategory = 'TRAVEL' | 'GAME' | 'STUDY'

export type Template = {
  key: string
  name: string
  categoryHint: GuideCategory
  starterMarkdown: string
}

export type GuideCard = {
  id: number
  authorId: number
  title: string
  category: GuideCategory
  templateKey?: string | null
  authorName: string
  createdAt: string
  likeCount: number
  favoriteCount: number
}

export type GuideDetail = {
  id: number
  authorId: number
  title: string
  category: GuideCategory
  templateKey?: string | null
  contentMarkdown: string
  authorName: string
  createdAt: string
  updatedAt: string
  likeCount: number
  favoriteCount: number
  liked: boolean
  favorited: boolean
  checkinCount?: number
  checkinToday?: boolean
}

export type CheckInRecord = {
  id: number
  guideId: number
  guideTitle: string
  checkinDate: string
  createdAt: string
}

export type Comment = { id: number; userName: string; content: string; createdAt: string }
export type CheckIn = { id: number; day: string; progress: number; note?: string | null; createdAt: string }

export type UserRole = 'USER' | 'ADMIN'

export type User = { id: number; username: string; displayName: string; role: UserRole }

