"""Builds the natural leather diary cover as inline SVG for the Deciary artboards."""

PALETTES = {
    'Ink':  dict(base='#2A2547', dark='#120F24', light='#433C6E', thread='#C9B8FA', ink='#EEEAF8', hi='#4D4680', lo='#07050F', m0='#FFE1D6', m1='#E0A08C', m2='#9A5A4A', label='Ink leather · rose-gold snap'),
    'Plum': dict(base='#5B3FB0', dark='#2E1C6B', light='#7A5ED6', thread='#D9CCFF', ink='#F3EEFF', hi='#8F7AE0', lo='#24145A', m0='#FFFFFF', m1='#C9C4D6', m2='#7C7690', label='Plum leather · silver snap'),
    'Tan':  dict(base='#A8693A', dark='#5E3517', light='#C98B55', thread='#F4E1C1', ink='#FFF6EA', hi='#D29A6A', lo='#4A2711', m0='#FFF1C2', m1='#D4A44A', m2='#8C6420', label='Natural tan · brass snap'),
}


def holes(prefix='lc'):
    """Palette keys mapped to {{prefix.key}} holes, for artboards that switch leather live."""
    return {k: '{{%s.%s}}' % (prefix, k) for k in PALETTES['Ink']}


def cover(k, c, w=358, h=150, name='Priya', sub='Only you can open this', name_size=44,
          blur=None, strap=True, radius=26):
    """One leather cover. k = unique id suffix; c = palette dict (literal colours or holes)."""
    bs = f' style="filter: {blur};"' if blur else ''
    sy = h / 2
    clip = (f'M10 0 H{w-radius} A{radius} {radius} 0 0 1 {w} {radius} V{h-radius} '
            f'A{radius} {radius} 0 0 1 {w-radius} {h} H10 A10 10 0 0 1 0 {h-10} V10 A10 10 0 0 1 10 0Z')
    st_r = radius - 6
    stitch = (f'M30 10 H{w-10-st_r} A{st_r} {st_r} 0 0 1 {w-10} {10+st_r} V{h-10-st_r} '
              f'A{st_r} {st_r} 0 0 1 {w-10-st_r} {h-10} H30')
    ny = sy + (6 if sub else name_size * 0.35)
    parts = [
        f'<svg aria-hidden="true" width="100%" height="100%" viewBox="0 0 {w} {h}" preserveAspectRatio="none" style="position: absolute; inset: 0; display: block;"><defs>',
        f'<filter id="gr{k}" x="0" y="0" width="100%" height="100%"><feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="3" seed="7" result="n"></feTurbulence><feColorMatrix in="n" type="matrix" values="0 0 0 0 0  0 0 0 0 0  0 0 0 0 0  0 0 0 0.55 -0.1" result="a"></feColorMatrix><feComposite in="a" in2="SourceGraphic" operator="in"></feComposite></filter>',
        f'<filter id="pb{k}" x="0" y="0" width="100%" height="100%"><feTurbulence type="turbulence" baseFrequency="0.08" numOctaves="2" seed="3" result="t"></feTurbulence><feDiffuseLighting in="t" surfaceScale="1.6" lighting-color="#ffffff" result="l"><feDistantLight azimuth="235" elevation="55"></feDistantLight></feDiffuseLighting><feComposite in="l" in2="SourceGraphic" operator="in"></feComposite></filter>',
        f'<radialGradient id="vg{k}" cx="0.42" cy="0.38" r="0.85"><stop offset="0" stop-color="{c["light"]}"></stop><stop offset="0.65" stop-color="{c["base"]}"></stop><stop offset="1" stop-color="{c["dark"]}"></stop></radialGradient>',
        f'<linearGradient id="sp{k}" x1="0" x2="1"><stop offset="0" stop-color="#000000" stop-opacity="0.35"></stop><stop offset="0.7" stop-color="#000000" stop-opacity="0.12"></stop><stop offset="1" stop-color="#ffffff" stop-opacity="0.08"></stop></linearGradient>',
        f'<linearGradient id="st{k}" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{c["light"]}"></stop><stop offset="1" stop-color="{c["dark"]}"></stop></linearGradient>',
        f'<radialGradient id="mt{k}" cx="0.35" cy="0.3" r="0.8"><stop offset="0" stop-color="{c["m0"]}"></stop><stop offset="0.55" stop-color="{c["m1"]}"></stop><stop offset="1" stop-color="{c["m2"]}"></stop></radialGradient>',
        f'<clipPath id="cl{k}"><path d="{clip}"></path></clipPath>',
        f'<filter id="sh{k}" x="-10%" y="-20%" width="120%" height="160%"><feDropShadow dx="0" dy="3" stdDeviation="3" flood-color="#000000" flood-opacity="0.35"></feDropShadow></filter>',
        f'</defs><g clip-path="url(#cl{k})">',
        f'<rect width="{w}" height="{h}" fill="url(#vg{k})"></rect>',
        f'<rect width="{w}" height="{h}" fill="#888888" filter="url(#pb{k})" opacity="0.22" style="mix-blend-mode: overlay;"></rect>',
        f'<rect width="{w}" height="{h}" fill="#000000" filter="url(#gr{k})" opacity="0.35"></rect>',
        f'<rect width="18" height="{h}" fill="url(#sp{k})"></rect>',
        f'<line x1="18.5" y1="0" x2="18.5" y2="{h}" stroke="#000000" stroke-opacity="0.25"></line>',
        f'<path d="{stitch}" fill="none" stroke="#000000" stroke-opacity="0.25" stroke-width="2.2" stroke-dasharray="5 4" transform="translate(0 1)"></path>',
        f'<path d="{stitch}" fill="none" stroke="{c["thread"]}" stroke-width="1.8" stroke-dasharray="5 4" stroke-linecap="round"></path>',
    ]
    if name:
        parts += [
            f'<text x="40" y="{ny+2:.0f}" font-family="Fraunces, Georgia, serif" font-weight="800" font-size="{name_size}" fill="{c["hi"]}" letter-spacing="-1"{bs}>{name}</text>',
            f'<text x="40" y="{ny:.0f}" font-family="Fraunces, Georgia, serif" font-weight="800" font-size="{name_size}" fill="{c["lo"]}" letter-spacing="-1"{bs}>{name}</text>',
            f'<text x="40" y="{ny-1:.0f}" font-family="Fraunces, Georgia, serif" font-weight="800" font-size="{name_size}" fill="{c["ink"]}" letter-spacing="-1"{bs}>{name}</text>',
        ]
        if sub:
            parts.append(f'<text x="41" y="{ny+30:.0f}" font-family="Fraunces, Georgia, serif" font-style="italic" font-weight="500" font-size="15" fill="{c["ink"]}" opacity="0.85">{sub}</text>')
    if strap:
        x0 = w - 98
        parts += [
            f'<g filter="url(#sh{k})">',
            f'<path d="M{x0} {sy-24} H{w} V{sy+24} H{x0} A24 24 0 0 1 {x0} {sy-24}Z" fill="url(#st{k})"></path>',
            f'<path d="M{x0} {sy-24} H{w} V{sy+24} H{x0} A24 24 0 0 1 {x0} {sy-24}Z" fill="#000000" filter="url(#gr{k})" opacity="0.35"></path>',
            f'<path d="M{x0+4} {sy-18} H{w} M{x0+4} {sy+18} H{w} M{x0+4} {sy-18} A18 18 0 0 0 {x0+4} {sy+18}" fill="none" stroke="{c["thread"]}" stroke-width="1.5" stroke-dasharray="4 3.5" opacity="0.9"></path>',
            f'<circle cx="{x0+14}" cy="{sy}" r="15" fill="url(#mt{k})"></circle>',
            f'<circle cx="{x0+14}" cy="{sy}" r="10.5" fill="none" stroke="#000000" stroke-opacity="0.22" stroke-width="1.2"></circle>',
            f'<circle cx="{x0+14}" cy="{sy}" r="15" fill="none" stroke="#ffffff" stroke-opacity="0.35" stroke-width="0.8"></circle>',
            f'<ellipse cx="{x0+9}" cy="{sy-6}" rx="4.5" ry="2.6" fill="#ffffff" opacity="0.6" transform="rotate(-30 {x0+9} {sy-6})"></ellipse>',
            '</g>',
        ]
    parts.append('</g></svg>')
    return ''.join(parts)


def snap_link(w=358, h=150, href='V2Lock.dc.html'):
    """Transparent 48px tap target over the press-stud: tapping the clasp locks the diary."""
    x0 = w - 98
    return (f'<a href="{href}" aria-label="Lock now" style="position: absolute; left: {x0-10}px; top: {h/2-24:.0f}px; '
            f'width: 48px; height: 48px; border-radius: 24px;"></a>')
