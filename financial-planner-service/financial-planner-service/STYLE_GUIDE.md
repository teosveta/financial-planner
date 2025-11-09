# 🎨 Visual Style Guide - Financial Planner Dashboard

## Color Palette

### Primary Colors

```
┌─────────────────────────────────────────┐
│ PRIMARY (#6366f1)                       │
│ Indigo - Main actions, focus states    │
│ Use for: Buttons, links, highlights    │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ SECONDARY (#8b5cf6)                     │
│ Purple - AI features, accents          │
│ Use for: AI badges, secondary actions  │
└─────────────────────────────────────────┘
```

### Status Colors

```
┌─────────────────────────────────────────┐
│ SUCCESS (#10b981)                       │
│ Green - Positive, savings, income      │
│ Use for: Success messages, savings     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ WARNING (#f59e0b)                       │
│ Amber - Caution, moderate priority     │
│ Use for: Warnings, medium priority     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ DANGER (#ef4444)                        │
│ Red - Urgent, overspending, high       │
│ Use for: Errors, urgent items, alerts  │
└─────────────────────────────────────────┘
```

### Neutrals

```
┌─────────────────────────────────────────┐
│ DARK (#1e293b)                          │
│ Slate - Headings, primary text         │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ GRAY (#64748b)                          │
│ Gray - Secondary text, labels          │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ LIGHT GRAY (#f1f5f9)                    │
│ Light - Backgrounds, subtle areas      │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ WHITE (#ffffff)                         │
│ White - Card backgrounds, clean areas  │
└─────────────────────────────────────────┘
```

---

## Typography

### Font Family
```
font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
```

### Size Scale

```
3rem (48px)    → Main headings (H1)
1.5rem (24px)  → Section headings (H2)
1.125rem (18px)→ Card titles (H4)
1rem (16px)    → Body text, buttons
0.875rem (14px)→ Labels, captions
```

### Font Weights

```
700 (Bold)     → Amounts, headings
600 (Semi-bold)→ Labels, titles
400 (Regular)  → Body text
```

---

## Spacing System

### Padding Scale

```
8px   → Compact elements (badges)
12px  → Form inputs vertical
16px  → Form inputs horizontal, small cards
20px  → Medium cards, buttons
25px  → Standard cards
30px  → Large cards, sections
```

### Margin Scale

```
10px  → Small gaps
15px  → Medium gaps between items
20px  → Card spacing in grids
30px  → Section spacing
40px  → Major section breaks
```

---

## Border Radius

```
10px  → Buttons, inputs, small cards
12px  → Medium elements
16px  → Large cards
20px  → Pills, badges (fully rounded)
```

---

## Shadows

### Light Shadow
```css
box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1),
            0 2px 4px -1px rgba(0, 0, 0, 0.06);
```
**Use for:** Cards, elevated elements

### Large Shadow
```css
box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1),
            0 10px 10px -5px rgba(0, 0, 0, 0.04);
```
**Use for:** Hover states, modals, important cards

---

## Icon Guidelines

### Icon Library
**Font Awesome 6.4.0** via CDN

### Icon Sizing

```
3rem (48px)    → Large feature icons
1.5rem (24px)  → Section headings
1rem (16px)    → Inline with text
```

### Common Icons

```
📈 fa-chart-line       → Analytics, financial
🤖 fa-robot           → AI features
👤 fa-user            → User-related
📅 fa-calendar        → Date/time
🔍 fa-search          → Search, analyze
💡 fa-lightbulb       → Recommendations
🧠 fa-brain           → AI insights
💰 fa-coins           → Money, savings
📉 fa-arrow-down      → Expenses
📈 fa-arrow-up        → Income
🐷 fa-piggy-bank      → Savings
⚙️ fa-sliders-h       → Settings
🔄 fa-spinner         → Loading
✓ fa-check-circle     → Success
⚠ fa-exclamation-...  → Warning/error
😊 fa-smile           → Positive
😐 fa-meh             → Neutral
😟 fa-frown           → Negative
```

---

## Component Styles

### Summary Card

```
┌─────────────────────────────────────┐
│  [Icon]  TITLE                      │
│                                     │
│  1,234.56 EUR      ← Big amount    │
│  Subtitle text     ← Small gray    │
└─────────────────────────────────────┘

Background: White (#ffffff)
Border-radius: 16px
Padding: 25px
Shadow: Light
Hover: Lift -5px + large shadow
```

### Recommendation Card

```
┌─────────────────────────────────────┐
│ [PRIORITY] Title                    │
│ ═══════════════════════════════════ │
│ Description text in gray...         │
│                                     │
│ 💰 Potential Savings: XX EUR        │
└─────────────────────────────────────┘

Background: Light gray (#f1f5f9)
Border-left: 4px solid [priority-color]
Border-radius: 12px
Padding: 20px
Hover: Lift + shadow, slide right 5px
```

### Priority Badge

```
┌──────┐
│ HIGH │  ← Red (#ef4444)
└──────┘

┌────────┐
│ MEDIUM │  ← Amber (#f59e0b)
└────────┘

┌─────┐
│ LOW │  ← Green (#10b981)
└─────┘

Padding: 4px 12px
Border-radius: 20px
Font-size: 0.75rem (12px)
Font-weight: 600
Text: Uppercase
```

### Button

```
┌─────────────────────┐
│ [Icon] Button Text  │
└─────────────────────┘

Primary: Blue background (#6366f1)
Secondary: Purple background (#8b5cf6)
Color: White
Padding: 12px 28px
Border-radius: 10px
Font-weight: 600
Hover: Darker + lift -2px
```

### Input Field

```
┌─────────────────────────────────────┐
│ [Label with icon]                   │
│ ┌─────────────────────────────────┐ │
│ │ Input text...                   │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘

Border: 2px solid #e2e8f0
Border-radius: 10px
Padding: 12px 16px
Focus: Blue border + glow shadow
```

---

## Layout Grid

### Summary Grid
```css
display: grid;
grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
gap: 20px;
```

**Result:**
```
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│ Card 1 │ │ Card 2 │ │ Card 3 │ │ Card 4 │
└────────┘ └────────┘ └────────┘ └────────┘
```

### Insights Grid
```css
display: grid;
grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
gap: 20px;
```

### Control Panel
```css
display: grid;
grid-template-columns: 2fr 1fr auto auto;
gap: 15px;
```

**Result:**
```
┌──────────────┐ ┌──────┐ ┌────────┐ ┌────────┐
│ User ID      │ │Period│ │Analyze │ │Category│
└──────────────┘ └──────┘ └────────┘ └────────┘
```

---

## Animation Timing

### Transitions
```css
transition: all 0.3s ease;
```

### Hover Effects
```css
transform: translateY(-5px);  /* Card lift */
transform: translateY(-2px);  /* Button press */
transform: translateX(5px);   /* Slide right */
```

### Loading Spinner
```css
@keyframes spin {
    to { transform: rotate(360deg); }
}
animation: spin 1s linear infinite;
```

---

## Responsive Breakpoints

### Mobile (<768px)
```css
@media (max-width: 768px) {
    /* Single column layouts */
    grid-template-columns: 1fr;
    
    /* Smaller text */
    .header h1 { font-size: 2rem; }
    
    /* Adjusted heights */
    .chart-container { height: 300px; }
}
```

---

## Usage Examples

### Creating a New Card
```html
<div class="summary-card">
    <div class="summary-card-header">
        <div class="summary-card-icon [type]">
            <i class="fas fa-[icon-name]"></i>
        </div>
        <div>
            <div class="summary-card-title">TITLE</div>
        </div>
    </div>
    <div class="summary-card-amount">Amount</div>
    <div class="summary-card-subtitle">Subtitle</div>
</div>
```

### Adding a Recommendation
```html
<div class="recommendation [high|medium|low]">
    <div class="recommendation-header">
        <span class="priority-badge [high|medium|low]">
            PRIORITY
        </span>
        <h4>Title</h4>
    </div>
    <p>Description...</p>
    <div class="savings-amount">
        <i class="fas fa-coins"></i>
        Potential Savings: XX EUR
    </div>
</div>
```

### Adding an Insight
```html
<div class="insight [positive|negative|neutral]">
    <div class="insight-icon">
        <i class="fas fa-[smile|meh|frown]"></i>
    </div>
    <p>Insight message...</p>
</div>
```

---

## Color Usage Guide

### When to Use Each Color

**Primary (Blue):**
- Main call-to-action buttons
- Links and interactive elements
- Focus states
- Primary information

**Secondary (Purple):**
- AI-related features
- Secondary buttons
- Alternative actions
- Special badges

**Success (Green):**
- Income indicators
- Savings highlights
- Positive insights
- Success messages
- Low priority items

**Warning (Amber):**
- Medium priority items
- Cautionary messages
- Neutral alerts
- Moderate concerns

**Danger (Red):**
- Expense indicators
- High priority items
- Error messages
- Urgent alerts
- Negative insights

**Gray:**
- Secondary text
- Subtle elements
- Neutral states
- Disabled items

---

## Accessibility Guidelines

### Contrast Ratios
- Text on white: 4.5:1 minimum
- Large text: 3:1 minimum
- UI components: 3:1 minimum

### Interactive Elements
- Minimum touch target: 44x44px
- Clear focus indicators
- Keyboard navigation support
- Screen reader friendly

### Color Independence
- Don't rely solely on color
- Use icons + text
- Include labels
- Provide alternative indicators

---

## Best Practices

### Do's ✅
- Use CSS variables for consistency
- Follow the spacing scale
- Include appropriate icons
- Add hover states
- Provide loading feedback
- Show error messages clearly
- Test on multiple screen sizes

### Don'ts ❌
- Don't use random colors
- Don't skip hover effects
- Don't ignore mobile layouts
- Don't use tiny touch targets
- Don't forget loading states
- Don't hide error messages
- Don't mix different design styles

---

## Quick Reference

### Most Used Values

**Colors:**
- Primary: `var(--primary)` or `#6366f1`
- White: `var(--white)` or `#ffffff`
- Gray: `var(--gray)` or `#64748b`

**Spacing:**
- Card padding: `25px` or `30px`
- Grid gap: `20px`
- Section margin: `30px`

**Shadows:**
- Default: `var(--shadow)`
- Hover: `var(--shadow-lg)`

**Border Radius:**
- Cards: `16px`
- Buttons: `10px`
- Inputs: `10px`

**Transitions:**
- Standard: `all 0.3s ease`

---

**Use this guide to maintain consistency across all UI elements!**

*Professional Design System*  
*Ready to Use*  
*Easy to Customize*
