# Product Context - User Personas & UX Goals

> Last updated: 2026-03-26

## Target Market

- **Location**: Switzerland (French-speaking)
- **Industry**: Media/Entertainment (Job monitoring for media indexing)
- **Language**: French

## User Personas

### Primary User: Job Search User
- Looking for employment opportunities
- Wants to search and save job offers
- Expects real-time updates on job analysis
- French-speaking
- Expects WCAG 2.1 AA accessibility

### Secondary User: System Administrator
- Monitors API health
- Manages deployments
- Technical background

## UX Goals

### Real-Time Experience
- Instant job search results (JSearch API)
- Real-time updates via SSE when analysis completes
- Visual status indicators

### Accessibility (WCAG 2.1 AA)
- Keyboard navigation
- Screen reader compatible
- Color contrast compliant
- Focus indicators

### User Experience
- Fast response times
- Clear error messages in French
- Intuitive navigation
- Mobile-responsive

## Features (Prototype)

1. **Job Search** - Search jobs via JSearch API
2. **Job Dashboard** - View and manage saved jobs
3. **Save Job** - Persist jobs to database
4. **AI Analysis** - Automatic analysis when saving (score, tags, summary)
5. **Real-time Updates** - SSE for analysis completion

## Workflow (Smart Cost)

1. User searches → Results displayed instantly (DISCOVERED)
2. User saves job → Persisted (SAVED) → Kafka message
3. IA Analyzer consumes → Analyzes → Produces result
4. Dashboard consumes result → Updates DB → SSE to frontend

## Constraints

- No AI/ML in production (per RFP) - simulated analysis
- Swiss data compliance
- High accessibility requirements
- API limits (RapidAPI)